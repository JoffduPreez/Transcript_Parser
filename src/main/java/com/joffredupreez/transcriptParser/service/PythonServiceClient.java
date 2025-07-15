package com.joffredupreez.transcriptParser.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class PythonServiceClient {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${python.service.url}")
    private String pythonServiceUrl;

    public TranscriptionJobResponse startTranscription(String filePath) {
        String url = pythonServiceUrl + "/transcribe";

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new FileSystemResource(filePath));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        return restTemplate.postForObject(url, requestEntity, TranscriptionJobResponse.class);
    }

    public SummarizationJobResponse startSummarization(String text) {
        String url = pythonServiceUrl + "/summarize";

        SummarizationRequest request = new SummarizationRequest(text);

        return restTemplate.postForObject(url, request, SummarizationJobResponse.class);
    }

    public TaskExtractionJobResponse startTaskExtraction(String text) {
        String url = pythonServiceUrl + "/extract-tasks";

        TaskExtractionRequest request = new TaskExtractionRequest(text);

        return restTemplate.postForObject(url, request, TaskExtractionJobResponse.class);
    }

    public JobStatusResponse checkJobStatus(String jobId) {
        String url = pythonServiceUrl + "/jobs/" + jobId;

        return restTemplate.getForObject(url, JobStatusResponse.class);
    }
}