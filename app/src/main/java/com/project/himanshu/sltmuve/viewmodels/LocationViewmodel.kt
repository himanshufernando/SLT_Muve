package com.project.himanshu.sltmuve.viewmodels

import androidx.lifecycle.*
import com.project.himanshu.sltmuve.data.dataModel.UserLocation
import com.project.himanshu.sltmuve.data.dataModel.VehicleType
import com.project.himanshu.sltmuve.repo.LocationRepo
import kotlinx.coroutines.Dispatchers

class LocationViewmodel : ViewModel(){

    var repo = LocationRepo()


    private val _userLocation = MutableLiveData<UserLocation>()
    val userLocation: LiveData<UserLocation> = _userLocation

    private val _vehicleTypeStatus = MutableLiveData<Boolean>()
    val vehicleTypeStatus: LiveData<Boolean> = _vehicleTypeStatus



    init {
        _vehicleTypeStatus.value = true

    }

    val liveLocation: LiveData<Boolean> = userLocation.switchMap { userLocation -> repo.updateLocationToFirebase(userLocation) }
    fun setLocationUpdate(userLocation: UserLocation){ _userLocation.value = userLocation }


    val vehicleTypeList: LiveData<List<VehicleType>> = vehicleTypeStatus.switchMap { it
        repo.getVehicleType()
    }


}