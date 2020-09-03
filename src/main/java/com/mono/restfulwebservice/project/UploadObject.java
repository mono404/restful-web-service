package com.mono.restfulwebservice.project;

import com.google.api.gax.longrunning.OperationFuture;
import com.google.cloud.speech.v1p1beta1.*;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.apache.commons.io.FilenameUtils;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;


public class UploadObject {

    String projectId;
    String bucketName;
    String objectName;
    String filePath;
    String result;

    public UploadObject(String projectId, String bucketName, String objectName, String filePath) throws Exception {

        this.projectId = projectId;
        this.bucketName = bucketName;
        this.objectName = objectName;
        this.filePath = filePath;

        String fileName;
        String extension = FilenameUtils.getExtension(filePath);

        if(extension.equals("wav") || extension.equals("raw"))
        {
            System.out.println("raw or wav 파일입니다");
            this.filePath = filePath;
        }
        else
        {
            System.out.println("wav파일이 아닙니다");
            TranslateWav translateWav = new TranslateWav(filePath);
            this.filePath = "./upload/" + translateWav.getResult();
        }

        Storage storage = StorageOptions.newBuilder().setProjectId(projectId).build().getService();
        BlobId blobId = BlobId.of(bucketName, objectName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();
        storage.create(blobInfo, Files.readAllBytes(Paths.get(this.filePath)));

        System.out.println(
                "File " + this.filePath + " uploaded to bucket " + bucketName + " as " + objectName);

        String target = "gs://" + bucketName + "/" + objectName;
        int speaker = 2;
        transcribeDiarization(target, speaker);

    }

    public String getLog() {
        return "File " + filePath + " uploaded to bucket " + bucketName + " as " + objectName;
    }

    public void transcribeDiarization(String fileName, int speaker)throws Exception {
        try (SpeechClient speechClient = SpeechClient.create()) {

            ArrayList<String> languageList = new ArrayList<>();
            languageList.add("ko-KR");

            SpeakerDiarizationConfig speakerDiarizationConfig =
                    SpeakerDiarizationConfig.newBuilder()
                            .setEnableSpeakerDiarization(true)
                            .setMinSpeakerCount(speaker)
                            .setMaxSpeakerCount(speaker)
                            .build();

            RecognitionConfig config = RecognitionConfig.newBuilder()
                    .setEncoding(RecognitionConfig.AudioEncoding.LINEAR16)
                    .setSampleRateHertz(16000)
                    .setLanguageCode("es-US")
                    .addAllAlternativeLanguageCodes(languageList)
                    .setEnableAutomaticPunctuation(true)
                    .setDiarizationConfig(speakerDiarizationConfig)
                    .build();

            RecognitionAudio audio = RecognitionAudio.newBuilder()
                    .setUri(fileName)
                    .build();

            OperationFuture<LongRunningRecognizeResponse, LongRunningRecognizeMetadata> response =
                    speechClient.longRunningRecognizeAsync(config, audio);

            LongRunningRecognizeResponse longRunningRecognizeResponse = response.get();

            SpeechRecognitionAlternative alternative =
                    longRunningRecognizeResponse
                            .getResults(longRunningRecognizeResponse.getResultsCount() - 1)
                            .getAlternatives(0);

            WordInfo wordInfo = alternative.getWords(0);
            int currentSpeakerTag = wordInfo.getSpeakerTag();

            StringBuilder speakerWords = new StringBuilder(
                    String.format("Speaker %d: %s", wordInfo.getSpeakerTag(), wordInfo.getWord()));

            for (int i = 1; i < alternative.getWordsCount(); i++) {
                wordInfo = alternative.getWords(i);
                if(i == 1) System.out.printf("[%s.%s] ", wordInfo.getStartTime().getSeconds(),wordInfo.getStartTime().getNanos() / 100000000);
                if (currentSpeakerTag == wordInfo.getSpeakerTag()) {
                    speakerWords.append(" ");
                    speakerWords.append(wordInfo.getWord());
                } else {
                    speakerWords.append(
                            String.format("\nSpeaker %d: %s", wordInfo.getSpeakerTag(), wordInfo.getWord()));
                    currentSpeakerTag = wordInfo.getSpeakerTag();
                }
            }

            System.out.println(speakerWords.toString());

            this.result = speakerWords.toString();
        }
    }

    public String getResult(){
        return result;
    }
}
