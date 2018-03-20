package main.java.com.company.java;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

public class EnvironmentUtils {

    final static String envKeyPrefix = "envKey_";
    protected static Document xmlDocument = null;


    public static void main(String[] args) throws Exception {
        String pathTofile = "C:\\Users\\Oleksandr_Karpenko1\\Documents\\XML_Parser\\src\\main\\resources\\test.xml";
        initXMLDocument(pathTofile);
        new EnvironmentUtils().getValuesByXpath();
       // getValueByXMLKeyFromTestResources("FirstReportedDate");
    }

    public void getValuesByXpath() {
        try {
            XPath xPath = XPathFactory.newInstance().newXPath();

            String Debtor = "/GetBankruptcyDataResponse/GetBankruptcyDataResult/TransactionId";
//            System.out.println(Debtor);
//            NodeList nodeList = (NodeList) xPath.compile(Debtor).evaluate(xmlDocument, XPathConstants.NODE);
//            for (int i = 0; i < nodeList.getLength(); i++) {
//                System.out.println(nodeList.item(i).getFirstChild().getNodeName());
//            }
                System.out.println("***************");

                Node node = (Node) xPath.compile(Debtor).evaluate(xmlDocument, XPathConstants.NODE);
                System.out.println(node.getNodeValue());
//                NodeList nodeList = node.getChildNodes();
//
//                    for (int i = 0; i < nodeList.getLength(); i++) {
//                        Node nod = nodeList.item(i);
//                        System.out.println(nodeList.item(i).getNodeName() + " : " + nod.getFirstChild().getNodeValue());
//                    }


        }
        catch (XPathExpressionException e) {
            e.printStackTrace();
        }
    }


    public static String getEnvironmentDependentValue(String stepValue) throws Exception {
        if (!stepValue.contains(envKeyPrefix)) {

            return stepValue;
        }
        String xmlKey = stepValue.replace(envKeyPrefix, "");

        return getValueByXMLKeyFromTestResources(xmlKey);
    }

    /**
     * Looks for the unique xml tag in the file defined by test.resources property and return its text value
     *
     * @param xmlKey- xml tag name
     * @return value from testResouces file
     * @throws Exception if the input xml key is not found or found more than once, exception is produced
     */
    public static String getValueByXMLKeyFromTestResources(String xmlKey) throws Exception {
        NodeList nList = xmlDocument.getElementsByTagName(xmlKey);

        isSingleNodeReturned(nList, xmlKey);

        String value = nList.item(0).getTextContent();
        return value;
    }

    public static void setValueByXMLKeyToTestResources(String xmlKey, String value) throws Exception {
        initXMLDocument(System.getProperty("test.resources"));
        NodeList nList = xmlDocument.getElementsByTagName(xmlKey);

        isSingleNodeReturned(nList, xmlKey);

        if (isCurrentXmlKeyValueOutdated(xmlKey, value)) {
            nList.item(0).setTextContent(value);
            updateXmlFile();
            System.out.printf("Node %s in test resource is updated with value: %s ", xmlKey, value);
        }
    }

    private static void isSingleNodeReturned(NodeList nList, String xmlKey) throws Exception {
        if (nList.getLength() == 0) {
            throw new Exception("envKey with name: " + xmlKey + " not found in the test resource file "
                    + System.getProperty("test.resources"));
        } else if (nList.getLength() > 1) {
            throw new Exception("Several envKey with name: " + xmlKey + " are found in the test resource file " + System.getProperty("test.resources")
                    + " Xml key name must be unique.");
        }
    }

    private static boolean isCurrentXmlKeyValueOutdated(String xmlKey, String value) throws ParserConfigurationException, SAXException, IOException {
        initXMLDocument(System.getProperty("test.resources"));
        NodeList nList = xmlDocument.getElementsByTagName(xmlKey);
        return !nList.item(0).getTextContent().equals(value);
    }

    protected static void initXMLDocument(String pathToFile) throws SAXException, IOException, ParserConfigurationException {
        InputStream is;
        DocumentBuilderFactory dbFactory;
        DocumentBuilder dBuilder;
        if (xmlDocument == null) {
            dbFactory = DocumentBuilderFactory.newInstance();
            dBuilder = dbFactory.newDocumentBuilder();
            try {
                is = new FileInputStream(new File(pathToFile));
                xmlDocument = dBuilder.parse(is);
                xmlDocument.getDocumentElement().normalize();
            }catch (IllegalArgumentException ia){
                System.out.println("Test resource was not succesfully initialized by clasloader. Will try to open as file using absolute path.");
                is = new BufferedInputStream(new FileInputStream(new File(pathToFile)));
                xmlDocument = dBuilder.parse(is);
                xmlDocument.getDocumentElement().normalize();
            }
        }
    }

    private static void updateXmlFile() throws TransformerException {
        TransformerFactory transformerFactory = TransformerFactory
                .newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(xmlDocument);
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        String path = classloader.getResource(System.getProperty("test.resources")).getPath();
        StreamResult result = new StreamResult(new File(path));
        transformer.transform(source, result);
    }

    public static String getFileFullPath(String packagePathToFile)
            throws SAXException, IOException, ParserConfigurationException {

        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        String[] pdfDocument = classloader.getResource(packagePathToFile).toString().split("file:/");

        return pdfDocument[1].replace("/", "\\");
    }

    public static Document getXmlDocument() {
        return xmlDocument;
    }
}