package com.slobo.master.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;

import com.github.fakemongo.Fongo;
import com.mongodb.Mongo;

@Configuration
public class MongoConfig extends AbstractMongoConfiguration
{

    @Override
    protected String getDatabaseName()
    {
        return "fongo-db";
    }

    @Override
    public Mongo mongo() throws Exception
    {
        return new Fongo(getDatabaseName()).getMongo();
    }

}
