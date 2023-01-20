package ru.netology.patient.service.medical;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import ru.netology.patient.entity.BloodPressure;
import ru.netology.patient.entity.HealthInfo;
import ru.netology.patient.entity.PatientInfo;
import ru.netology.patient.repository.PatientInfoFileRepository;
import ru.netology.patient.service.alert.SendAlertServiceImpl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

class MedicalServiceImplTest {

    private SendAlertServiceImpl sendAlertService;

    private MedicalService medicalService;

    private String id;


    @BeforeEach
    void setUp() {
        PatientInfoFileRepository patientInfoFileRepository = Mockito.mock(PatientInfoFileRepository.class);
        this.sendAlertService = Mockito.mock(SendAlertServiceImpl.class);

        PatientInfo patientInfo = new PatientInfo(UUID.randomUUID().toString(), "Иван", "Петров", LocalDate.of(1980, 11, 26),
                new HealthInfo(new BigDecimal("36.6"), new BloodPressure(120, 80)));

        Mockito.when(patientInfoFileRepository.add(patientInfo)).thenReturn(patientInfo.getId());

        this.id = patientInfoFileRepository.add(patientInfo);

        Mockito.when(patientInfoFileRepository.getById(id)).thenReturn(patientInfo);

        this.medicalService = new MedicalServiceImpl(patientInfoFileRepository, sendAlertService);
    }

    @Test
    void checkBloodPressureWarning() {
        BloodPressure currentPressure = new BloodPressure(60, 120);
        medicalService.checkBloodPressure(id, currentPressure);

        ArgumentCaptor<String> argument = ArgumentCaptor.forClass(String.class);
        verify(sendAlertService, Mockito.times(1)).send(argument.capture());

        assertEquals(String.format("Warning, patient with id: %s, need help", id), argument.getValue());
    }

    @Test
    void checkBloodPressureOk() {
        BloodPressure currentPressure = new BloodPressure(120, 80);
        medicalService.checkBloodPressure(id, currentPressure);

        ArgumentCaptor<String> argument = ArgumentCaptor.forClass(String.class);
        verify(sendAlertService, Mockito.never()).send(argument.capture());
    }

    @Test
    void checkTemperatureWarning() {
        BigDecimal currentTemperature = new BigDecimal("34.0");
        medicalService.checkTemperature(id, currentTemperature);

        ArgumentCaptor<String> argument = ArgumentCaptor.forClass(String.class);
        verify(sendAlertService, Mockito.times(1)).send(argument.capture());

        assertEquals(String.format("Warning, patient with id: %s, need help", id), argument.getValue());
    }


    @Test
    void checkTemperatureOk() {
        BigDecimal currentTemperature = new BigDecimal("36.65");
        medicalService.checkTemperature(id, currentTemperature);

        ArgumentCaptor<String> argument = ArgumentCaptor.forClass(String.class);
        verify(sendAlertService, Mockito.never()).send(argument.capture());
    }

}