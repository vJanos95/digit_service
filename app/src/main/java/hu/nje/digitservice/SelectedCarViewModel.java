package hu.nje.digitservice;


import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import hu.nje.digitservice.database.Car;


public class SelectedCarViewModel extends ViewModel {

    private final MutableLiveData<Car> selectedCar = new MutableLiveData<>();

    public void select(Car car) {
        selectedCar.setValue(car);
    }

    public LiveData<Car> getSelectedCar() {
        return selectedCar;
        }
}