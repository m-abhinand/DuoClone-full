package com.example.duoclone.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "tech_terms")
public class TechTerm {
    @Id
    private String id;
    private String term;
    private String definition;

    public TechTerm() {}

    public TechTerm(String id, String term, String definition) {
        this.id = id;
        this.term = term;
        this.definition = definition;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTerm() { return term; }
    public void setTerm(String term) { this.term = term; }

    public String getDefinition() { return definition; }
    public void setDefinition(String definition) { this.definition = definition; }
}
