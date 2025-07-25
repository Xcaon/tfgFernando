package com.example.tfgfernando.activities.FormScreen

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.health.connect.client.HealthConnectClient
import com.example.tfgfernando.ui.theme.TfgFernandoTheme
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.DistanceRecord
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import androidx.health.connect.client.records.metadata.Device
import androidx.health.connect.client.records.metadata.Metadata
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.response.ReadRecordResponse
import androidx.health.connect.client.response.ReadRecordsResponse
import androidx.health.connect.client.time.TimeRangeFilter
import androidx.health.connect.client.units.Energy
import androidx.health.connect.client.units.Length
import androidx.lifecycle.lifecycleScope
import com.example.tfgfernando.navigation.MyApp
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset

const val providerPackageName = "com.google.android.apps.healthdata"

@AndroidEntryPoint
class MainActivity : ComponentActivity() {


    val PERMISSIONS =
        setOf(
            // Permisos para HeartRate
            HealthPermission.getReadPermission(HeartRateRecord::class),
            HealthPermission.getWritePermission(HeartRateRecord::class),

            // Permisos para Steps (pasos)
            HealthPermission.getReadPermission(StepsRecord::class),
            HealthPermission.getWritePermission(StepsRecord::class),

            // Permisos para Distance (distancia recorrida)
            HealthPermission.getReadPermission(DistanceRecord::class),
            HealthPermission.getWritePermission(DistanceRecord::class),

            // Permisos para Calories (calorías quemadas)
            HealthPermission.getReadPermission(TotalCaloriesBurnedRecord::class),
            HealthPermission.getWritePermission(TotalCaloriesBurnedRecord::class),

            HealthPermission.getReadPermission(TotalCaloriesBurnedRecord::class),
            HealthPermission.getWritePermission(TotalCaloriesBurnedRecord::class)
        )


    val requestPermissionActivityContract =
        PermissionController.createRequestPermissionResultContract()

    // Launcher para solicitar permisos
    private val requestPermissions: ActivityResultLauncher<Set<String>> =
        registerForActivityResult(PermissionController.createRequestPermissionResultContract()) { granted ->
            if (granted.containsAll(PERMISSIONS)) {
                // Permisos concedidos
                Log.i("HealthConnect", "Permisos concedidos")
            } else {
                // Permisos denegados
                Log.e("HealthConnect", "Permisos denegados")
            }
        }
    companion object {
        lateinit var healthConnectClient: HealthConnectClient
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Comprobamos que la app de Health Connect este instalada
        if (healthCheckStatus(context = this) != null) {
            healthConnectClient = healthCheckStatus(context = this)!!
        }

        if (healthConnectClient != null) {
            checkPermissionsAndRun(healthConnectClient)
        }


//            var pasosLeidos = readStepsByTimeRange(
//                healthConnectClient!!,
//                Instant.now().minus(Duration.ofHours(1)),
//                Instant.now()
//            )

        // Esto nos sirve para simular datos
        // insertSteps(healthConnectClient)


        // Aqui arranca la aplicacion
        setContent {
            TfgFernandoTheme {
                // Iniciamos el menu inferior
                MyApp()
            }
        }

    }



    fun checkPermissionsAndRun(healthConnectClient: HealthConnectClient) {
        lifecycleScope.launch {
            val granted = healthConnectClient.permissionController.getGrantedPermissions()
            if (granted.containsAll(PERMISSIONS)) {
                Log.i("HealthConnect", "Permisos concedidos")
                // Insertamos datos sabiendo que los permisos estan okey
                insertSteps(healthConnectClient)
            } else {
                requestPermissions.launch(PERMISSIONS)
            }
        }
    }


    // Comprobar que la app existe en el dispositivo para acceder al almacenamiento
    fun healthCheckStatus(context: Activity): HealthConnectClient? {
        val availabilityStatus = HealthConnectClient.getSdkStatus(context, providerPackageName)
        val healthConnectClient = HealthConnectClient.getOrCreate(context)

        if (availabilityStatus == HealthConnectClient.SDK_UNAVAILABLE) {
            Log.e("HealthConnect", "Health Connect is not available")
        } else if (availabilityStatus == HealthConnectClient.SDK_AVAILABLE) {
            Log.i("HealthConnect", "Health Connect is available")
        }

        if (availabilityStatus == HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED) {
            Log.e("HealthConnect", "Health Connect App is not available")
            // Optionally redirect to package installer to find a provider, for example:
            val uriString =
                "market://details?id=$providerPackageName&url=healthconnect%3A%2F%2Fonboarding"
            context.startActivity(
                Intent(Intent.ACTION_VIEW).apply {
                    setPackage("com.android.vending")
//                    Intent.setData = uriString.toUri() TODO() Esta mierda ha dejado de funcionar por la cara
                    putExtra("overlay", true)
                    putExtra("callerId", context.packageName)
                }
            )
            return null
        }
        return healthConnectClient

    }

    // Introducimos datos de prueba
    fun insertSteps(healthConnectClient: HealthConnectClient?) {
        lifecycleScope.launch {
            val endTime = Instant.now()
            val startTime = endTime.minus(Duration.ofMinutes(15))


            val device = Device(
                manufacturer = "Ejemplo", // Puedes poner la información que desees
                model = "Ejemplo Watch",   // Puedes poner la información que desees
                type = Device.TYPE_WATCH,  // Puedes poner la información que desees
            )

            try {
                val stepsRecord = StepsRecord(
                    count = 120,
                    startTime = startTime,
                    endTime = endTime,
                    startZoneOffset = ZoneOffset.UTC,
                    endZoneOffset = ZoneOffset.UTC,
                    metadata = Metadata.autoRecorded(
                        device
                    ),
                )

                val distanceRecord = DistanceRecord(
                    distance = Length.meters(200.0),
                    startTime = startTime,
                    endTime = endTime,
                    startZoneOffset = ZoneOffset.UTC,
                    endZoneOffset = ZoneOffset.UTC,
                    metadata = Metadata.autoRecorded(
                        device
                    )
                )

                // Calorías quemadas: 150 kcal
                val caloriesRecord = TotalCaloriesBurnedRecord(
                    energy = Energy.kilocalories(150.0),
                    startTime = startTime,
                    endTime = endTime,
                    startZoneOffset = ZoneOffset.UTC,
                    endZoneOffset = ZoneOffset.UTC,
                    metadata = Metadata.autoRecorded(
                        device
                    )
                )

                healthConnectClient!!.insertRecords(
                    listOf(stepsRecord, distanceRecord, caloriesRecord)
                )

            } catch (e: Exception) {
                // Run error handling here
                Log.e("HealthConnect", "Error inserting steps", e)
            }
        }
    }
}

suspend fun readStepsByTimeRange(
    healthConnectClient: HealthConnectClient,
    startTime: Instant,
    endTime: Instant
): ReadRecordsResponse<StepsRecord> {
    return try {
        healthConnectClient.readRecords(
            ReadRecordsRequest(
                StepsRecord::class,
                timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
            )
        )
    } catch (e: Exception) {
        Log.e("HealthConnect", "Error reading steps", e)
        throw e // o maneja el error de otra forma si quieres devolver algo alternativo
    }
}

suspend fun readDistanceByTimeRange(
    healthConnectClient: HealthConnectClient,
    startTime: Instant,
    endTime: Instant
): ReadRecordsResponse<DistanceRecord> {
    return try {
        healthConnectClient.readRecords(
            ReadRecordsRequest(
                DistanceRecord::class,
                timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
            )
        )
    } catch (e: Exception) {
        Log.e("HealthConnect", "Error reading distance", e)
        throw e
    }
}

suspend fun readCaloriesByTimeRange(
    healthConnectClient: HealthConnectClient,
    startTime: Instant,
    endTime: Instant
): ReadRecordsResponse<TotalCaloriesBurnedRecord> {
    return try {
        healthConnectClient.readRecords(
            ReadRecordsRequest(
                TotalCaloriesBurnedRecord::class,
                timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
            )
        )
    } catch (e: Exception) {
        Log.e("HealthConnect", "Error reading calories", e)
        throw e
    }
}


