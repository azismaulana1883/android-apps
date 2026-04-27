package com.example.qcinline

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.qcinline.ui.theme.QCInlineTheme
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.google.android.gms.location.LocationServices
import java.io.File

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            QCInlineTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val viewModel: QCViewModel = viewModel()
                    val navController = rememberNavController()

                    NavHost(navController = navController, startDestination = "layer1") {
                        composable("layer1") { Layer1Screen(navController, viewModel) }
                        composable("layer2") { Layer2Screen(navController, viewModel) }
                        composable("layer3") { Layer3Screen(navController, viewModel) }
                    }
                }
            }
        }
    }
}

// --- Layer 1 ---
@Composable
fun Layer1Screen(navController: NavController, viewModel: QCViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Pilih Round", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(32.dp))

        for (i in 1..5) {
            Button(
                onClick = {
                    viewModel.selectedRound.value = i
                    navController.navigate("layer2")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Round $i", fontSize = 18.sp)
            }
        }
    }
}

// --- Layer 2 ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Layer2Screen(navController: NavController, viewModel: QCViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Data Operator", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Text("Round: ${viewModel.selectedRound.value}", fontSize = 16.sp, color = Color.Gray)
        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = viewModel.nik.value,
            onValueChange = { viewModel.nik.value = it },
            label = { Text("NIK") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = viewModel.operatorName.value,
            onValueChange = { viewModel.operatorName.value = it },
            label = { Text("Nama Operator") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                if (viewModel.nik.value.isNotBlank() && viewModel.operatorName.value.isNotBlank()) {
                    navController.navigate("layer3")
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Next", fontSize = 18.sp)
        }
    }
}

// --- Layer 3 ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Layer3Screen(navController: NavController, viewModel: QCViewModel) {
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(false) }
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false)
        val coarseGranted = permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false)
        
        if (fineGranted || coarseGranted) {
            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    if (location != null) {
                        viewModel.latitude.value = location.latitude
                        viewModel.longitude.value = location.longitude
                        Toast.makeText(context, "Location updated: ${location.latitude}, ${location.longitude}", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: SecurityException) {
                e.printStackTrace()
            }
        } else {
            Toast.makeText(context, "Wajib izinkan lokasi!", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            locationPermissionLauncher.launch(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
            )
        } else {
            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    if (location != null) {
                        viewModel.latitude.value = location.latitude
                        viewModel.longitude.value = location.longitude
                    }
                }
            } catch (e: SecurityException) { }
        }
    }

    LaunchedEffect(viewModel.submissionResult.value) {
        viewModel.submissionResult.value?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            if (it.startsWith("Success")) {
                navController.popBackStack("layer1", false)
            }
            viewModel.submissionResult.value = null
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Konfirmasi") },
            text = { Text("Kamu yakin semua data sudah benar? Lokasi saat ini akan ikut dikirim.") },
            confirmButton = {
                TextButton(onClick = {
                    showDialog = false
                    viewModel.submitData(context)
                }) {
                    Text("Yes, Lanjut")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("No, Cek Lagi")
                }
            }
        )
    }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.addDefect() },
                icon = { Icon(Icons.Filled.Add, "Add Defect") },
                text = { Text("Add Defect") }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Text("Data QC & Defect", fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 16.dp))

            LazyColumn(
                modifier = Modifier.weight(1f)
            ) {
                itemsIndexed(viewModel.defects) { index, defect ->
                    DefectCard(index, defect, viewModel)
                }
            }

            Button(
                onClick = { showDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
                    .height(56.dp),
                enabled = !viewModel.isSubmitting.value,
                shape = RoundedCornerShape(12.dp)
            ) {
                if (viewModel.isSubmitting.value) {
                    CircularProgressIndicator(color = Color.White)
                } else {
                    Text("Finish", fontSize = 18.sp)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DefectCard(index: Int, defect: DefectData, viewModel: QCViewModel) {
    val context = LocalContext.current
    var tempImageUri by remember { mutableStateOf<Uri?>(null) }
    
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempImageUri != null) {
            viewModel.addPhotoToDefect(index, tempImageUri!!)
            tempImageUri = null
        }
    }

    fun createImageUri(): Uri {
        val imageFile = File(context.cacheDir, "qc_img_${System.currentTimeMillis()}.jpg").apply {
            createNewFile()
        }
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            imageFile
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Defect #${index + 1}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = defect.line,
                onValueChange = { viewModel.updateDefect(index, defect.copy(line = it)) },
                label = { Text("Line") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = defect.defectType,
                onValueChange = { viewModel.updateDefect(index, defect.copy(defectType = it)) },
                label = { Text("Jenis Defect") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = defect.defectArea,
                onValueChange = { viewModel.updateDefect(index, defect.copy(defectArea = it)) },
                label = { Text("Area Terkena Defect") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = defect.qtyInspect,
                    onValueChange = { viewModel.updateDefect(index, defect.copy(qtyInspect = it)) },
                    label = { Text("Qty Inspect") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = defect.qtyDefect,
                    onValueChange = { viewModel.updateDefect(index, defect.copy(qtyDefect = it)) },
                    label = { Text("Qty Defect") },
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Photos Section
            Text("Fotos (${defect.photoUris.size})", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            
            // Grid of photos or horizontal scroll
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(defect.photoUris) { uri ->
                    AsyncImage(
                        model = uri,
                        contentDescription = "Defect Photo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.LightGray)
                    )
                }
                
                item {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.LightGray)
                            .clickable { 
                            val uri = createImageUri()
                            tempImageUri = uri
                            cameraLauncher.launch(uri)
                        },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Add, "Add Photo", tint = Color.Gray)
                    }
                }
            }
        }
    }
}