package com.project.himanshu.sltmuve.repo

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.FirebaseDatabase
import com.project.himanshu.sltmuve.data.dataModel.UserLocation
import com.project.himanshu.sltmuve.data.dataModel.VehicleType


class LocationRepo {

    val myRef = FirebaseDatabase.getInstance().getReference("Location")

    companion object {
        private val TAG = LocationRepo::class.java.simpleName
    }

    fun updateLocationToFirebase(userLocation : UserLocation) : LiveData<Boolean>{
        Log.i(TAG, "updateLocationToFirebase ")
        val locationUpdateRespond = MutableLiveData<Boolean>()
        val newLocation = myRef.push()
        newLocation.child("latitude").setValue(userLocation.latitude)
        newLocation.child("longitude").setValue(userLocation.longitude)
            .addOnSuccessListener {
                Log.i(TAG, "updateLocationToFirebase success")
                locationUpdateRespond.postValue(true)
            }
            .addOnFailureListener {
                Log.i(TAG, "updateLocationToFirebase fail")
                locationUpdateRespond.postValue(false)
            }
        return locationUpdateRespond
    }

     fun getVehicleType() : MutableLiveData<List<VehicleType>>{

         val typeRspond = MutableLiveData<List<VehicleType>>()

        var type1 = VehicleType(101101,"Mini","https://img.gaadicdn.com/images/car-images/930x620/Tata/Tata-Nano/4080/Pearl-White.jpg",true)
        var type2 = VehicleType(101102,"Classic","https://img.gaadicdn.com/images/car-images/930x620/Tata/Tata-Nano/4080/Pearl-White.jpg",false)
        var type3 = VehicleType(101103,"Grand","https://img.gaadicdn.com/images/car-images/930x620/Tata/Tata-Nano/4080/Pearl-White.jpg",false)
        var type4 = VehicleType(101104,"Plus","https://img.gaadicdn.com/images/car-images/930x620/Tata/Tata-Nano/4080/Pearl-White.jpg",false)
        val vehicleTypeList = listOf(type1, type2,type3, type4)
         typeRspond.postValue(vehicleTypeList)

        return typeRspond
    }



}