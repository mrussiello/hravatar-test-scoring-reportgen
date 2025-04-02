/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2score.xml;

import com.tm2score.global.STException;
import com.tm2score.service.LogService;
import com.tm2score.imo.xml.Clicflic;
import com.tm2builder.sim.xml.SimJ;
import com.tm2score.api.AssessmentResult;
import com.tm2score.findly.xml.Scores;
import java.io.StringReader;
import java.io.StringWriter;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.UnmarshalException;
import jakarta.xml.bind.Unmarshaller;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

/**
 *
 * @author Mike
 */
public class JaxbUtils
{

    public static Clicflic ummarshalImoResultXml( String xml ) throws Exception
    {
        JAXBContext jc = JAXBContext.newInstance( "com.tm2score.imo.xml" );

        Unmarshaller u = jc.createUnmarshaller();

        try
        {
            // xml = XmlUtils.stripNonValidXMLCharacters( xml );
            
            // xml = XmlUtils.sanitizeXmlChars( xml );
            XMLInputFactory xif = XMLInputFactory.newFactory();
            xif.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
            xif.setProperty(XMLInputFactory.SUPPORT_DTD, false);
            XMLStreamReader xsr = xif.createXMLStreamReader(new StringReader(xml));            
            return (Clicflic) u.unmarshal( xsr );
        }

        catch( UnmarshalException e )
        {
            LogService.logIt( "JaxbUtils.ummarshalImoResultXml() " + e.toString() + " AAA.1 Will try again after cleaning. " + xml );

            if( xml == null || xml.length()==0 )
                throw new STException( e );
        }
        catch( Exception e )
        {
            LogService.logIt( e, "JaxbUtils.ummarshalImoResultXml() AAA.2 Will try again after cleaning. " + xml );

            if( xml == null || xml.length()==0 )
                throw new STException( e );
        }

        try
        {
            xml = XmlUtils.stripNonValidXMLCharacters( xml );
            XMLInputFactory xif = XMLInputFactory.newFactory();
            xif.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
            xif.setProperty(XMLInputFactory.SUPPORT_DTD, false);
            XMLStreamReader xsr = xif.createXMLStreamReader(new StringReader(xml));            
            return (Clicflic) u.unmarshal( xsr );
        }
        catch( UnmarshalException e )
        {
            LogService.logIt( "JaxbUtils.ummarshalImoResultXml() " + e.toString() + " BBB.1 Exception after after cleaning. Cleaned XML=" + xml );
            throw new STException( e );
        }
        catch( Exception e )
        {
            LogService.logIt( e, "JaxbUtils.ummarshalImoResultXml() BBB.2 Exception after after cleaning. Cleaned XML=" + xml );
            throw new STException( e );
        }

    }
    
    public static String marshalImoResultXml( Clicflic clicflic ) throws Exception
    {
        try
        {
            JAXBContext jc = JAXBContext.newInstance( "com.tm2score.imo.xml" );

            Marshaller u = jc.createMarshaller();
            StringWriter sw = new StringWriter();
            u.marshal(clicflic, sw );
            return sw.toString();
        }

        catch( Exception e )
        {
            LogService.logIt( e, "JaxbUtils.marshalImoResultXml() " );
            throw new STException( e );
        }
    }
    

    public static SimJ ummarshalSimDescriptorXml( String xml ) throws Exception
    {
        try
        {
            xml = XmlUtils.stripNonValidXMLCharacters( xml );

            JAXBContext jc = JAXBContext.newInstance( "com.tm2builder.sim.xml" );

            Unmarshaller u = jc.createUnmarshaller();

            return (SimJ) u.unmarshal( new StringReader( xml ) );
        }

        catch( Exception e )
        {
            LogService.logIt( e, "JaxbUtils.ummarshalSimDescriptorXml() " + xml );

            throw new STException( e );
        }
    }

    public static com.tm2score.essay.copyscape.xml.Response ummarshalCopyScapeXml( String xml ) throws Exception
    {
        JAXBContext jc = JAXBContext.newInstance( "com.tm2score.essay.copyscape.xml" );

        Unmarshaller u = jc.createUnmarshaller();

        try
        {
            // xml = XmlUtils.stripNonValidXMLCharacters( xml );
            XMLInputFactory xif = XMLInputFactory.newFactory();
            xif.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
            xif.setProperty(XMLInputFactory.SUPPORT_DTD, false);
            XMLStreamReader xsr = xif.createXMLStreamReader(new StringReader(xml));            
            return (com.tm2score.essay.copyscape.xml.Response) u.unmarshal( xsr );
        }
        catch( Exception e )
        {
            LogService.logIt( e, "JaxbUtils.ummarshalCopyScapeXml()  AAA. Trying again after cleaning. " + xml );

            if( xml == null || xml.length()==0 )
                throw new STException( e );
        }

        try
        {
            xml = XmlUtils.stripNonValidXMLCharacters( xml );
            XMLInputFactory xif = XMLInputFactory.newFactory();
            xif.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
            xif.setProperty(XMLInputFactory.SUPPORT_DTD, false);
            XMLStreamReader xsr = xif.createXMLStreamReader(new StringReader(xml));            
            return (com.tm2score.essay.copyscape.xml.Response) u.unmarshal( xsr );
        }
        catch( Exception e )
        {
            LogService.logIt( e, "JaxbUtils.ummarshalCopyScapeXml() BBB Cleaned XML=" + xml );
            throw new STException( e );
        }

    }


    public static Scores ummarshalFindlyScoreXml( String xml ) throws Exception
    {
        JAXBContext jc = JAXBContext.newInstance( "com.tm2score.findly.xml" );

        Unmarshaller u = jc.createUnmarshaller();

        try
        {
            // xml = XmlUtils.stripNonValidXMLCharacters( xml );

            XMLInputFactory xif = XMLInputFactory.newFactory();
            xif.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
            xif.setProperty(XMLInputFactory.SUPPORT_DTD, false);
            XMLStreamReader xsr = xif.createXMLStreamReader(new StringReader(xml));            
            return (Scores) u.unmarshal( xsr );
        }

        catch( Exception e )
        {
            LogService.logIt( e, "JaxbUtils.ummarshalFindlyScoreXml()  AAA. Trying again after cleaning." + xml );

            if( xml == null || xml.length()==0 )
                throw new STException( e );
        }

        try
        {
            xml = XmlUtils.stripNonValidXMLCharacters( xml );

            return (Scores) u.unmarshal( new StringReader( xml ) );
        }

        catch( Exception e )
        {
            LogService.logIt( e, "JaxbUtils.ummarshalFindlyScoreXml() BBB Cleaned XML=" + xml );

                throw new STException( e );
        }

    }


    public static String marshalAssessmentResultXml( AssessmentResult assessmentResult ) throws Exception
    {
        try
        {            
            JAXBContext jc = JAXBContext.newInstance(AssessmentResult.class );//JAXBContext.newInstance( "com.tm2test.api" );
            Marshaller u = jc.createMarshaller();

            u.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

            StringWriter sw = new StringWriter();
            u.marshal(assessmentResult, sw );
            return sw.toString();
        }

        catch( Exception e )
        {
            LogService.logIt( e, "JaxbUtils.marshalAssessmentResultXml() orgId=" + assessmentResult.getClientId().getIdValue().getValue() +", ClientOrderId=" + assessmentResult.getClientOrderId().getIdValue().getValue() );

            throw new STException( e );
        }
    }


}
