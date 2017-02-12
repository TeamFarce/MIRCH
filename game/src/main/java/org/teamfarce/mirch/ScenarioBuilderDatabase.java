package org.teamfarce.mirch;

import java.sql.*;
import java.util.HashMap;
import java.util.HashSet;

public class ScenarioBuilderDatabase
{
    public HashMap<Integer, DataMeansClue> means;
    public HashMap<Integer, DataResource> resources;
    public HashMap<Integer, DataClue> clues;
    public HashMap<Integer, DataQuestioningStyle> questioningStyles;
    public HashMap<Integer, DataMotive> motives;
    public HashMap<Integer, DataCharacter> characters;

    public ScenarioBuilderDatabase()
    {
        means = new HashMap<>();
        resources = new HashMap<>();
        clues = new HashMap<>();
        questioningStyles = new HashMap<>();
        motives = new HashMap<>();
        characters = new HashMap<>();
    }
    public ScenarioBuilderDatabase(String databaseName) throws SQLException
    {
        this();

        Connection sqlConn = DriverManager.getConnection("jdbc:sqlite:" + databaseName);
        Statement sqlStmt = sqlConn.createStatement();

        ResultSet rsMeans = sqlStmt.executeQuery("SELECT * FROM clues WHERE is_means == 1");
        while (rsMeans.next()) {
            DataMeansClue singleMeans = new DataMeansClue();
            singleMeans.id = rsMeans.getInt("id");
            singleMeans.name = rsMeans.getString("name");
            singleMeans.description = rsMeans.getString("description");
            singleMeans.resource = "clock.png";
            means.put(singleMeans.id, singleMeans);
        }

        ResultSet rsResource = sqlStmt.executeQuery("SELECT * FROM resources");
        while (rsResource.next()) {
            DataResource resource = new DataResource();
            resource.id = rsResource.getInt("id");
            resource.filename = rsResource.getString("filename");
            resource.characters = new HashSet<>();
            resources.put(resource.id, resource);
        }

        ResultSet rsClue = sqlStmt.executeQuery("SELECT * FROM clues WHERE is_means != 1");
        while (rsClue.next()) {
            DataClue clue = new DataClue();
            clue.id = rsClue.getInt("id");
            clue.name = rsClue.getString("name");
            clue.description = rsClue.getString("description");
            clue.resource = "clock.png";
            clues.put(clue.id, clue);
        }

        ResultSet rsQuestioningStyle = sqlStmt.executeQuery("SELECT * FROM questioning_styles");
        while (rsQuestioningStyle.next()) {
            DataQuestioningStyle questioningStyle = new DataQuestioningStyle();
            questioningStyle.id = rsQuestioningStyle.getInt("id");
            questioningStyle.description = rsQuestioningStyle.getString("description");
            questioningStyles.put(questioningStyle.id, questioningStyle);
        }

        ResultSet rsMotive = sqlStmt.executeQuery("SELECT * FROM motives");
        while (rsMotive.next()) {
            DataMotive motive = new DataMotive();
            motive.id = rsMotive.getInt("id");
            motive.description = rsMotive.getString("description");
            motives.put(motive.id, motive);
        }

        ResultSet rsCharacter = sqlStmt.executeQuery("SELECT * FROM characters");
        while (rsCharacter.next()) {
            DataCharacter character = new DataCharacter();
            character.id = rsCharacter.getInt("id");
            character.name = rsCharacter.getString("name");
            character.description = rsCharacter.getString("description");
            character.selectionWeight = rsCharacter.getInt("selection_weight");
            character.resource = resources.get(rsCharacter.getInt("resource"));
            character.posKiller = rsCharacter.getInt("posKiller") == 1;
            character.resource.characters.add(character);
            characters.put(character.id, character);
        }


    }

    public class DataMeansClue
    {
        public int id;
        public String name;
        public String description;
        public String resource;

    }

    public class DataResource
    {
        public int id;
        public String filename;
        public HashSet<DataCharacter> characters;
    }

    public class DataClue
    {
        public int id;
        public String name;
        public String description;
        public String resource;
    }

    public class DataQuestioningStyle
    {
        public int id;
        public String description;
    }

    public class DataMotive
    {
        public int id;
        public String description;
        public HashSet<DataClue> clues;
    }


    public class DataCharacter
    {
        public int id;
        public String name;
        public String description;
        public int selectionWeight;
        public boolean posKiller;
        public DataResource resource;
    }

}
