package edu.hm.hafner.coverage.parser;

import java.io.Reader;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import edu.hm.hafner.coverage.CoverageParser;
import edu.hm.hafner.coverage.FileNode;
import edu.hm.hafner.coverage.ModuleNode;
import edu.hm.hafner.coverage.Node;
import edu.hm.hafner.coverage.PackageNode;
import edu.hm.hafner.util.FilteredLog;
import edu.hm.hafner.util.PathUtil;
import edu.hm.hafner.util.SecureXmlParserFactory;
import edu.hm.hafner.util.SecureXmlParserFactory.ParsingException;

/**
 * A parser which parses reports made by OpenCover into a Java Object Model.
 *
 */
public class OpenCoverParser extends CoverageParser {

    private static final long serialVersionUID = 1L;

    private static final PathUtil PATH_UTIL = new PathUtil();

    /** XML elements. */
    private static final QName MODULE = new QName("Module");
    private static final QName CLASS = new QName("Class");
    private static final QName METHOD = new QName("Method");
    private static final QName CLASS_NAME = new QName("FullName");
    private static final QName METHOD_NAME = new QName("Name");
    private static final QName MODULE_NAME = new QName("ModuleName");
    private static final QName FILE = new QName("File");

    private static final QName UID = new QName("uid");
    private static final QName FULL_PATH = new QName("fullPath");
    private static final QName CLASS_COMPLEXITY = new QName("maxCyclomaticComplexity");
    private static final QName METHOD_COMPLEXITY = new QName("cyclomaticComplexity");

    @Override
    protected ModuleNode parseReport(final Reader reader, final FilteredLog log) {
        try {
            var eventReader = new SecureXmlParserFactory().createXmlEventReader(reader);
            var root = new ModuleNode("-");
            boolean isEmpty = true;
            while (eventReader.hasNext()) {
                XMLEvent event = eventReader.nextEvent();
                if (event.isStartElement()) {
                    var startElement = event.asStartElement();
                    var tagName = startElement.getName();
                    if (MODULE.equals(tagName)) {
                        readPackage(eventReader, root, startElement, log);
                        isEmpty = false;
                    }
                }
            }
            if (isEmpty) {
                throw new NoSuchElementException("No coverage information found in the specified file.");
            }
            return root;
        }
        catch (XMLStreamException exception) {
            throw new ParsingException(exception);
        }
    }

    private void readPackage(final XMLEventReader reader, final ModuleNode root,
            final StartElement currentStartElement, final FilteredLog log) throws XMLStreamException {

        Map<String, String> files = new LinkedHashMap<>();
        PackageNode packageNode = null;
        while (reader.hasNext()) {
            XMLEvent event = reader.nextEvent();
            if (event.isStartElement()) {
                var nextElement = event.asStartElement();
                if (CLASS.equals(nextElement.getName())) {
                    readClass(reader, nextElement, log);
                }
                else if (FILE.equals(nextElement.getName())) {
                    var fileName = getValueOf(nextElement, FULL_PATH);
                    var uid = getValueOf(nextElement, UID);
                    var relativePath = PATH_UTIL.getRelativePath(fileName);
                    files.put(uid, relativePath);
                }
                else if (MODULE_NAME.equals(nextElement.getName())) {
                    String packageName = reader.nextEvent().asCharacters().getData();
                    packageNode = root.findOrCreatePackageNode(packageName);
                }
            }
        }

        // Creating all file nodes
        for (var entry : files.entrySet()) {
            packageNode.findOrCreateFileNode(getFileName(entry.getValue()), getTreeStringBuilder().intern(entry.getValue()));
        }
    
    }

    private void readFile(final XMLEventReader reader, final ModuleNode root,
            final StartElement currentStartElement, final FilteredLog log) throws XMLStreamException {

        PackageNode packageNode = null;
        while (reader.hasNext()) {
            XMLEvent event = reader.nextEvent();
            if (event.isStartElement()) {
                var nextElement = event.asStartElement();
                if (CLASS.equals(nextElement.getName())) {
                    readClass(reader, nextElement, log);
                }
                else if (MODULE_NAME.equals(nextElement.getName())) {
                    String packageName = reader.nextEvent().asCharacters().getData();
                    packageNode = root.findOrCreatePackageNode(packageName);
                }
            }
        }
    }

    private void readClass(final XMLEventReader reader, final StartElement parentElement, final FilteredLog log) throws XMLStreamException {
        while (reader.hasNext()) {
            XMLEvent event = reader.nextEvent();
            if (event.isStartElement()) {
                var nextElement = event.asStartElement();
                if (CLASS_NAME.equals(nextElement.getName())) {
                    String className = reader.nextEvent().asCharacters().getData();
                }
            }
        }
    }

    private int readComplexity(final String c) {
        try {
            return Math.round(Float.parseFloat(c)); // some reports use float values
        }
        catch (NumberFormatException ignore) {
            return 0;
        }
    }

    private Node createClassNode(final FileNode file, final StartElement parentElement) {
        // Read summary has name
        return file.createClassNode(UUID.randomUUID().toString());
    }

    protected static String getValueOf(final StartElement element, final QName attribute) {
        return getOptionalValueOf(element, attribute).orElseThrow(
                () -> new NoSuchElementException(String.format(
                        "Could not obtain attribute '%s' from element '%s'", attribute, element)));
    }

    private String getFileName(final String relativePath) {
        var path = Paths.get(PATH_UTIL.getAbsolutePath(relativePath)).getFileName();
        if (path == null) {
            return relativePath;
        }
        return path.toString();
    }

}
