package com.ttn.punchingSystem;

import com.ttn.punchingSystem.model.PunchingDetailsDTO;
import com.ttn.punchingSystem.model.WorkScheduleDetails;
import com.ttn.punchingSystem.service.CsvReaderService;
import junit.framework.TestCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.ResponseEntity;

import java.io.File;
import java.net.URL;
import java.util.*;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;

@RunWith(MockitoJUnitRunner.Silent.class)
public class PunchingSystemTest extends TestCase {

    @Spy
    @InjectMocks
    private CsvReaderService csvReaderService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void read_csvFileSuccess() throws Exception{
        ClassLoader classLoader = getClass().getClassLoader();
        URL resource = classLoader.getResource("19Oct2024_punchdetails.csv");
        assert resource != null;
        String filePath = new File(resource.getFile()).getAbsolutePath();
        String mockFileName = "19Oct2024_punchdetails.csv";
        doNothing().when(csvReaderService).saveLogsToRepository(anyList());
        ResponseEntity<List<PunchingDetailsDTO>> response = csvReaderService.readCsvFile(filePath);
        assertEquals(200, response.getStatusCodeValue());
    }
}
