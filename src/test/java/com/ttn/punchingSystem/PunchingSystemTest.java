package com.ttn.punchingSystem;

import com.ttn.punchingSystem.model.PunchingDetails;
import com.ttn.punchingSystem.model.PunchingDetailsDTO;
import com.ttn.punchingSystem.model.WorkScheduleDetails;
import com.ttn.punchingSystem.repository.PunchLogRepository;
import com.ttn.punchingSystem.service.CsvReaderService;
import com.ttn.punchingSystem.utils.AppConstant;
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.Silent.class)
public class PunchingSystemTest extends TestCase {

    @Spy
    @InjectMocks
    private CsvReaderService csvReaderService;

    @Mock
    private PunchLogRepository punchLogRepository;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void readCsvFileSuccess() throws Exception{
        ClassLoader classLoader = getClass().getClassLoader();
        URL resource = classLoader.getResource("19Oct2024_punchdetails.csv");
        assert resource != null;
        String filePath = new File(resource.getFile()).getAbsolutePath();
        String mockFileName = "19Oct2024_punchdetails.csv";
        doNothing().when(csvReaderService).saveLogsToRepository(anyList());
        ResponseEntity<List<PunchingDetailsDTO>> response = csvReaderService.readCsvFile(filePath);
        assertEquals(200, response.getStatusCodeValue());
        List<PunchingDetailsDTO> punchDetails = response.getBody();
        assertNotNull(punchDetails);
        assertEquals(8, punchDetails.size());
        List<PunchingDetailsDTO> expectedDetails = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            br.readLine(); // Skip header
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                expectedDetails.add(new PunchingDetailsDTO(data[0], data[1]));
            }
        }
        for (int i = 0; i < punchDetails.size(); i++) {
            PunchingDetailsDTO actual = punchDetails.get(i);
            PunchingDetailsDTO expected = expectedDetails.get(i);
            assertEquals(expected.getUserEmail(), actual.getUserEmail());
            assertEquals(expected.getPunchTime(), actual.getPunchTime());
        }
    }

    @Test
    public void testInvalidFileName(){
        String invalidFileName = "19Oct20_punch.csv";
        Exception exception = assertThrows(IllegalArgumentException.class, () ->{
            csvReaderService.validateFileName(invalidFileName);
        });
        assertEquals("Invalid file name format. Expected format: 01Jan2024_punchdetails.csv", exception.getMessage());
    }

    @Test
    public void testSaveProcessedPunchLogs() throws Exception {
        Map<String, List<Date>> mockUserPunchTimes = new HashMap<>();
        SimpleDateFormat sdf = new SimpleDateFormat(AppConstant.DATE_FORMAT);
        mockUserPunchTimes.put("abc@tothenew.com", Arrays.asList(sdf.parse("19 Oct 2024 09:00 AM"), sdf.parse("19 Oct 2024 05:00 PM")));
        csvReaderService.saveProcessedPunchLogs(mockUserPunchTimes);
        verify(punchLogRepository, times(1)).save(any(PunchingDetails.class));
    }

}
