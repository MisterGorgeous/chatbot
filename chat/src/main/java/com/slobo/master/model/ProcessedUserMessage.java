package com.slobo.master.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Map;

@Document
public class ProcessedUserMessage
{
    @Id
    private String id;
    private String message;
    private String responseOnTheMessage;
    private Map<String, Integer> posStatistics;
    private int sentimentScore;

    public ProcessedUserMessage(String message, Map<String, Integer> posStatistics, int sentimentScore)
    {
        this.message = message;
        this.posStatistics = posStatistics;
        this.sentimentScore = sentimentScore;
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getResponseOnTheMessage()
    {
        return responseOnTheMessage;
    }

    public void setResponseOnTheMessage(String responseOnTheMessage)
    {
        this.responseOnTheMessage = responseOnTheMessage;
    }

    public String getMessage()
    {
        return message;
    }

    public void setMessage(String message)
    {
        this.message = message;
    }

    public Map<String, Integer> getPosStatistics()
    {
        return posStatistics;
    }

    public void setPosStatistics(Map<String, Integer> posStatistics)
    {
        this.posStatistics = posStatistics;
    }

    public int getSentimentScore()
    {
        return sentimentScore;
    }

    public void setSentimentScore(int sentimentScore)
    {
        this.sentimentScore = sentimentScore;
    }

}
