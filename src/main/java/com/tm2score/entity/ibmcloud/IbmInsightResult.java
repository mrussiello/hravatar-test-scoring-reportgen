package com.tm2score.entity.ibmcloud;



import com.tm2score.ibmcloud.SentinoResult;
import com.tm2score.service.LogService;
import java.io.Serializable;
import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Transient;


/*
Sample Sentino Results.
{
    "text": "I am very calm and steady. I get along with almost everyone. I genuinely like working with other people. I share my feelings openly. I am open to new experiences. I always take the time needed to understand a situation. I don't like to hurt other people's feelings. I generally keep my cool in challenging situations. I work very hard. I am friendly and courteous. I always think of others. I am always willing to help a friend.",
    "inventories": [
        "big5",
        "hexaco",
        "cpi",
        "6fpq"
    ],
    "scoring": {
        "6fpq": {
            "achievement-striving": {
                "quantile": 0.829,
                "score": 1.0,
                "confidence": 0.386,
                "confidence_text": "normal"
            },
            "adaptability": {
                "quantile": 0.859,
                "score": 1.0,
                "confidence": 0.51,
                "confidence_text": "high"
            },
            "adventurousness": {
                "quantile": 0.738,
                "score": 1.0,
                "confidence": 0.181,
                "confidence_text": "low"
            },
            "agreeableness": {
                "quantile": 0.858,
                "score": 1.0,
                "confidence": 0.506,
                "confidence_text": "high"
            },
            "calmness": {
                "quantile": 0.82,
                "score": 1.0,
                "confidence": 0.356,
                "confidence_text": "normal"
            },
            "comprehension": {
                "quantile": 0.679,
                "score": 1.0,
                "confidence": 0.111,
                "confidence_text": "low"
            },
            "conservatism": {
                "quantile": 0.67,
                "score": 0.728,
                "confidence": 0.176,
                "confidence_text": "low"
            },
            "culture": {
                "quantile": 0.804,
                "score": 1.0,
                "confidence": 0.311,
                "confidence_text": "normal"
            },
            "deliberateness": {
                "quantile": 0.743,
                "score": 0.782,
                "confidence": 0.327,
                "confidence_text": "normal"
            },
            "docility": {
                "quantile": 0.475,
                "score": -0.117,
                "confidence": 0.031,
                "confidence_text": "very low"
            },
            "exhibitionism": {
                "quantile": 0.704,
                "score": 1.0,
                "confidence": 0.138,
                "confidence_text": "low"
            },
            "extraversion": {
                "quantile": 0.825,
                "score": 1.0,
                "confidence": 0.37,
                "confidence_text": "normal"
            },
            "gregariousness": {
                "quantile": 0.856,
                "score": 1.0,
                "confidence": 0.493,
                "confidence_text": "normal"
            },
            "independence": {
                "quantile": 0.487,
                "score": -0.048,
                "confidence": 0.238,
                "confidence_text": "normal"
            },
            "industriousness": {
                "quantile": 0.763,
                "score": 1.0,
                "confidence": 0.221,
                "confidence_text": "normal"
            },
            "intellectual openness": {
                "quantile": 0.776,
                "score": 1.0,
                "confidence": 0.246,
                "confidence_text": "normal"
            },
            "leadership": {
                "quantile": 0.736,
                "score": 0.901,
                "confidence": 0.221,
                "confidence_text": "normal"
            },
            "methodicalness": {
                "quantile": 0.761,
                "score": 1.0,
                "confidence": 0.218,
                "confidence_text": "normal"
            },
            "orderliness": {
                "quantile": 0.703,
                "score": 1.0,
                "confidence": 0.137,
                "confidence_text": "low"
            },
            "playfulness": {
                "quantile": 0.755,
                "score": 1.0,
                "confidence": 0.208,
                "confidence_text": "normal"
            },
            "reclusiveness": {
                "quantile": 0.265,
                "score": -0.834,
                "confidence": 0.258,
                "confidence_text": "normal"
            },
            "resourcefulness": {
                "quantile": 0.838,
                "score": 1.0,
                "confidence": 0.417,
                "confidence_text": "normal"
            },
            "self-sufficiency": {
                "quantile": 0.649,
                "score": 0.529,
                "confidence": 0.256,
                "confidence_text": "normal"
            },
            "unpretentiousness": {
                "quantile": 0.555,
                "score": 0.287,
                "confidence": 0.062,
                "confidence_text": "low"
            }
        },
        "big5": {
            "agreeableness": {
                "quantile": 0.87,
                "score": 1.0,
                "confidence": 0.567,
                "confidence_text": "high"
            },
            "conscientiousness": {
                "quantile": 0.766,
                "score": 1.0,
                "confidence": 0.227,
                "confidence_text": "normal"
            },
            "extraversion": {
                "quantile": 0.851,
                "score": 1.0,
                "confidence": 0.473,
                "confidence_text": "normal"
            },
            "neuroticism": {
                "quantile": 0.184,
                "score": -1.0,
                "confidence": 0.343,
                "confidence_text": "normal"
            },
            "openness": {
                "quantile": 0.766,
                "score": 1.0,
                "confidence": 0.227,
                "confidence_text": "normal"
            }
        },
        "cpi": {
            "adventurousness": {
                "quantile": 0.832,
                "score": 1.0,
                "confidence": 0.395,
                "confidence_text": "normal"
            },
            "amiability": {
                "quantile": 0.833,
                "score": 1.0,
                "confidence": 0.401,
                "confidence_text": "normal"
            },
            "assertiveness": {
                "quantile": 0.786,
                "score": 1.0,
                "confidence": 0.268,
                "confidence_text": "normal"
            },
            "calmness": {
                "quantile": 0.79,
                "score": 0.916,
                "confidence": 0.345,
                "confidence_text": "normal"
            },
            "competence": {
                "quantile": 0.825,
                "score": 1.0,
                "confidence": 0.372,
                "confidence_text": "normal"
            },
            "complexity": {
                "quantile": 0.815,
                "score": 1.0,
                "confidence": 0.341,
                "confidence_text": "normal"
            },
            "comprehension": {
                "quantile": 0.707,
                "score": 1.0,
                "confidence": 0.141,
                "confidence_text": "low"
            },
            "depth": {
                "quantile": 0.88,
                "score": 1.0,
                "confidence": 0.636,
                "confidence_text": "high"
            },
            "disorder": {
                "quantile": 0.35,
                "score": -0.858,
                "confidence": 0.086,
                "confidence_text": "low"
            },
            "dominance": {
                "quantile": 0.26,
                "score": -1.0,
                "confidence": 0.184,
                "confidence_text": "low"
            },
            "dutifulness": {
                "quantile": 0.765,
                "score": 0.914,
                "confidence": 0.276,
                "confidence_text": "normal"
            },
            "forcefulness": {
                "quantile": 0.83,
                "score": 1.0,
                "confidence": 0.389,
                "confidence_text": "normal"
            },
            "good-nature": {
                "quantile": 0.848,
                "score": 1.0,
                "confidence": 0.456,
                "confidence_text": "normal"
            },
            "happiness": {
                "quantile": 0.859,
                "score": 1.0,
                "confidence": 0.51,
                "confidence_text": "high"
            },
            "insight": {
                "quantile": 0.809,
                "score": 1.0,
                "confidence": 0.324,
                "confidence_text": "normal"
            },
            "intellect": {
                "quantile": 0.723,
                "score": 1.0,
                "confidence": 0.161,
                "confidence_text": "low"
            },
            "introversion": {
                "quantile": 0.265,
                "score": -1.0,
                "confidence": 0.177,
                "confidence_text": "low"
            },
            "liberalism": {
                "quantile": 0.589,
                "score": 0.51,
                "confidence": 0.085,
                "confidence_text": "low"
            },
            "optimism": {
                "quantile": 0.841,
                "score": 1.0,
                "confidence": 0.43,
                "confidence_text": "normal"
            },
            "planfulness": {
                "quantile": 0.827,
                "score": 1.0,
                "confidence": 0.379,
                "confidence_text": "normal"
            },
            "poise": {
                "quantile": 0.831,
                "score": 1.0,
                "confidence": 0.392,
                "confidence_text": "normal"
            },
            "politeness": {
                "quantile": 0.872,
                "score": 1.0,
                "confidence": 0.581,
                "confidence_text": "high"
            },
            "responsibility": {
                "quantile": 0.885,
                "score": 1.0,
                "confidence": 0.67,
                "confidence_text": "high"
            },
            "security": {
                "quantile": 0.849,
                "score": 1.0,
                "confidence": 0.461,
                "confidence_text": "normal"
            },
            "self-control": {
                "quantile": 0.553,
                "score": 0.205,
                "confidence": 0.217,
                "confidence_text": "normal"
            },
            "self-discipline": {
                "quantile": 0.817,
                "score": 1.0,
                "confidence": 0.345,
                "confidence_text": "normal"
            },
            "self-efficacy": {
                "quantile": 0.796,
                "score": 1.0,
                "confidence": 0.289,
                "confidence_text": "normal"
            },
            "sentimentality": {
                "quantile": 0.672,
                "score": 0.524,
                "confidence": 0.383,
                "confidence_text": "normal"
            },
            "sociability": {
                "quantile": 0.841,
                "score": 1.0,
                "confidence": 0.427,
                "confidence_text": "normal"
            },
            "stability": {
                "quantile": 0.829,
                "score": 1.0,
                "confidence": 0.385,
                "confidence_text": "normal"
            },
            "temperance": {
                "quantile": 0.821,
                "score": 0.938,
                "confidence": 0.434,
                "confidence_text": "normal"
            },
            "timidity": {
                "quantile": 0.602,
                "score": 0.428,
                "confidence": 0.181,
                "confidence_text": "low"
            },
            "tolerance": {
                "quantile": 0.87,
                "score": 1.0,
                "confidence": 0.567,
                "confidence_text": "high"
            }
        },
        "hexaco": {
            "aesthetic appreciation": {
                "quantile": 0.759,
                "score": 1.0,
                "confidence": 0.215,
                "confidence_text": "normal"
            },
            "anxiety": {
                "quantile": 0.228,
                "score": -1.0,
                "confidence": 0.238,
                "confidence_text": "normal"
            },
            "creativity": {
                "quantile": 0.78,
                "score": 1.0,
                "confidence": 0.255,
                "confidence_text": "normal"
            },
            "dependence": {
                "quantile": 0.436,
                "score": -0.218,
                "confidence": 0.28,
                "confidence_text": "normal"
            },
            "diligence": {
                "quantile": 0.808,
                "score": 1.0,
                "confidence": 0.32,
                "confidence_text": "normal"
            },
            "expressiveness": {
                "quantile": 0.729,
                "score": 1.0,
                "confidence": 0.169,
                "confidence_text": "low"
            },
            "fairness": {
                "quantile": 0.815,
                "score": 1.0,
                "confidence": 0.34,
                "confidence_text": "normal"
            },
            "fearfulness": {
                "quantile": 0.259,
                "score": -0.881,
                "confidence": 0.241,
                "confidence_text": "normal"
            },
            "flexibility": {
                "quantile": 0.845,
                "score": 1.0,
                "confidence": 0.443,
                "confidence_text": "normal"
            },
            "forgiveness": {
                "quantile": 0.854,
                "score": 1.0,
                "confidence": 0.486,
                "confidence_text": "normal"
            },
            "gentleness": {
                "quantile": 0.856,
                "score": 1.0,
                "confidence": 0.492,
                "confidence_text": "normal"
            },
            "greed avoidance": {
                "quantile": 0.614,
                "score": 0.587,
                "confidence": 0.059,
                "confidence_text": "low"
            },
            "inquisitiveness": {
                "quantile": 0.726,
                "score": 1.0,
                "confidence": 0.165,
                "confidence_text": "low"
            },
            "liveliness": {
                "quantile": 0.846,
                "score": 1.0,
                "confidence": 0.45,
                "confidence_text": "normal"
            },
            "modesty": {
                "quantile": 0.662,
                "score": 0.701,
                "confidence": 0.172,
                "confidence_text": "low"
            },
            "organization": {
                "quantile": 0.66,
                "score": 0.938,
                "confidence": 0.094,
                "confidence_text": "low"
            },
            "patience": {
                "quantile": 0.824,
                "score": 1.0,
                "confidence": 0.368,
                "confidence_text": "normal"
            },
            "perfectionism": {
                "quantile": 0.681,
                "score": 1.0,
                "confidence": 0.113,
                "confidence_text": "low"
            },
            "prudence": {
                "quantile": 0.771,
                "score": 0.892,
                "confidence": 0.311,
                "confidence_text": "normal"
            },
            "sentimentality": {
                "quantile": 0.773,
                "score": 0.778,
                "confidence": 0.47,
                "confidence_text": "normal"
            },
            "sincerity": {
                "quantile": 0.766,
                "score": 1.0,
                "confidence": 0.227,
                "confidence_text": "normal"
            },
            "sociability": {
                "quantile": 0.849,
                "score": 1.0,
                "confidence": 0.463,
                "confidence_text": "normal"
            },
            "social boldness": {
                "quantile": 0.797,
                "score": 1.0,
                "confidence": 0.292,
                "confidence_text": "normal"
            },
            "unconventionality": {
                "quantile": 0.45,
                "score": -0.243,
                "confidence": 0.14,
                "confidence_text": "low"
            }
        }
    },
    "lang": "en"
}

*/


@Entity
@Table( name = "ibminsightresult" )
@NamedQueries( {
    @NamedQuery( name = "IbmInsightResult.findByTestEventId", query = "SELECT o FROM IbmInsightResult AS o WHERE o.testEventId=:testEventId" )
} )
public class IbmInsightResult implements Serializable
{
    @Transient
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY )
    @Column( name = "ibminsightresultid" )
    private long ibmInsightResultId;

    @Column( name = "testeventid" )
    private long testEventId;

    @Column( name = "versionid" )
    private String versionId;

    
    /*
     0=IBM Insight
     1=Sentino
    */
    @Column( name = "resulttypeid" )
    private int resultTypeId;

    
    @Column( name = "resultstatustypeid" )
    private int resultStatusTypeId;

    @Column( name = "wordcount" )
    private int wordCount;

    @Column( name = "resultjson" )
    private String  resultJson;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="lastupdate")
    private Date lastUpdate;

        
    @Override
    public String toString() {
        return "IbmInsightResult{" + "ibmInsightResultId=" + ibmInsightResultId + ", testEventId=" + testEventId + '}';
    }

    
    
    

    public long getIbmInsightResultId() {
        return ibmInsightResultId;
    }

    public void setIbmInsightResultId(long ibmInsightResultId) {
        this.ibmInsightResultId = ibmInsightResultId;
    }

    public long getTestEventId() {
        return testEventId;
    }

    public void setTestEventId(long testEventId) {
        this.testEventId = testEventId;
    }

    public int getResultStatusTypeId() {
        return resultStatusTypeId;
    }

    public void setResultStatusTypeId(int resultStatusTypeId) {
        this.resultStatusTypeId = resultStatusTypeId;
    }

    public int getWordCount() {
        return wordCount;
    }

    public void setWordCount(int wordCount) {
        this.wordCount = wordCount;
    }

    public String getResultJson() {
        return resultJson;
    }

    public void setResultJson(String resultJson) {
        this.resultJson = resultJson;
    }

    public String getVersionId() {
        return versionId;
    }

    public void setVersionId(String versionId) {
        this.versionId = versionId;
    }

    public Date getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public int getResultTypeId() {
        return resultTypeId;
    }

    public void setResultTypeId(int resultTypeId) {
        this.resultTypeId = resultTypeId;
    }
    
    
    
}
