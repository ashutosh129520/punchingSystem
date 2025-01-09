package com.ttn.punchingSystem;

import com.ttn.punchingSystem.model.PunchingDetails;
import com.ttn.punchingSystem.model.PunchingDetailsDTO;
import com.ttn.punchingSystem.repository.PunchLogRepository;
import com.ttn.punchingSystem.service.CsvReaderService;
import com.ttn.punchingSystem.utils.AppConstant;
import com.ttn.punchingSystem.utils.CsvValidationException;
import junit.framework.TestCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.io.*;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertThrows;
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


    public ResponseInputStream<GetObjectResponse> testReadCsvFileFromS3(String fileName) {
        S3Client s3Client = S3Client.builder()
                .region(Region.AP_SOUTH_1)
                .build();
        String bucketName = "punchingsystembucket";
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .build();
        ResponseInputStream<GetObjectResponse> s3Object = s3Client.getObject(getObjectRequest);
        return s3Object;
    }

    @Test
    public void testCsvFileWithMissingHeaderAndValues() {
        String fileName = "07Jan2025_punchdetails.csv";
        ResponseInputStream<GetObjectResponse> s3Object = testReadCsvFileFromS3(fileName);
        List<PunchingDetailsDTO> punchDataList = new ArrayList<>();
        List<String> errorList = new ArrayList<>();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(s3Object))) {
                csvReaderService.readCsvFileIntoDTO(punchDataList, br, errorList, fileName);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        assertFalse(errorList.isEmpty());
    }

    @Test
    public void testCsvData() throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        URL resource = classLoader.getResource("08Jan2025_punchdetails.csv");
        assert resource != null;
        String filePath = new File(resource.getFile()).getAbsolutePath();
        String fileName = "08Jan2025_punchdetails.csv";
        ResponseInputStream<GetObjectResponse> s3Object = testReadCsvFileFromS3(fileName);
        List<PunchingDetailsDTO> punchingDetailsList = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(s3Object))) {
            String line;
            br.readLine(); // Skip header
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length == 2) {
                    punchingDetailsList.add(new PunchingDetailsDTO(data[0], data[1]));
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        List<PunchingDetailsDTO> expectedDetails = new ArrayList<>();
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(filePath))) {
            String lines;
            bufferedReader.readLine(); // Skip header
            while ((lines = bufferedReader.readLine()) != null) {
                String[] data = lines.split(",");
                expectedDetails.add(new PunchingDetailsDTO(data[0], data[1]));
            }
        }
        assertNotNull(punchingDetailsList);
        assertEquals(11, punchingDetailsList.size());
        for (int i = 0; i < punchingDetailsList.size(); i++) {
            PunchingDetailsDTO actual = punchingDetailsList.get(i);
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
    public void testFileNotFoundInS3() {
        String fileName = "10Jan2025_punchdetails.csv.csv";
        try{
            testReadCsvFileFromS3(fileName);
            fail("No suck file exist in S3");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("The specified key does not exist"));
        }
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
