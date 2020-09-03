package com.mono.restfulwebservice.project;

import com.google.api.gax.longrunning.OperationFuture;
import com.google.cloud.speech.v1p1beta1.LongRunningRecognizeMetadata;
import com.google.cloud.speech.v1p1beta1.LongRunningRecognizeResponse;
import com.google.cloud.speech.v1p1beta1.RecognitionAudio;
import com.google.cloud.speech.v1p1beta1.RecognitionConfig;
import com.google.cloud.speech.v1p1beta1.RecognitionConfig.AudioEncoding;

import com.google.cloud.speech.v1p1beta1.SpeakerDiarizationConfig;
import com.google.cloud.speech.v1p1beta1.SpeechClient;
import com.google.cloud.speech.v1p1beta1.SpeechRecognitionAlternative;
import com.google.cloud.speech.v1p1beta1.WordInfo;
import java.util.ArrayList;


public class WavToTextInGC {
    public static void main(String[] args) throws Exception {

        String fileName = "gs://ai_stenographer/test12.raw";
        int speaker = 2;
        transcribeDiarization(fileName, speaker);
    }

    public static void transcribeDiarization(String fileName, int speaker)throws Exception {
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
                    .setEncoding(AudioEncoding.LINEAR16)
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

        }
    }
}