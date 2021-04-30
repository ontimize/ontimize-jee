package com.imatia.jee.server.test.spring.namespace;

import java.util.List;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.ontimize.jee.common.spring.parser.DefinitionParserUtil;

/**
 * Returns a list of files that are in a directory. The list may be limited by nesting a
 * core-commons:fileFilter tag that will do the intersection of all the fileFilters vs what is in
 * the directory.
 * <p/>
 *
 * Also supports the nesting of beans, idref, ref, and value tags that return File objects (the
 * value tag's value will be converted to a new File)
 *
 * <p/>
 * Examples: <br/>
 *
 * Get all the files in the current directory <core-commons:fileList directory="."/>
 *
 * Get all the files in the current directory that end in XML <core-commons:fileList directory=".">
 * <core-commons:fileFilter> <bean class="org.apache.commons.io.filefilter.RegexFileFilter">
 * <constructor-arg value=".*.xml"/> </bean> </core-commons:fileFilter> </core-commons:fileList>
 *
 * Get all the files in the current directory that end in XML (specify the fileList separately)
 * <core-commons:fileList> <core-commons:fileFilter>
 * <bean class="org.apache.commons.io.filefilter.RegexFileFilter"> <constructor-arg value=".*.xml"/>
 * </bean> </core-commons:fileFilter> <core-commons:fileList directory="."/>
 * </core-commons:fileList>
 *
 * Get all files in the /tmp and /something directory that end in .xml
 * <core-commons:fileList directory="/tmp"> <core-commons:fileFilter>
 * <bean class="org.apache.commons.io.filefilter.RegexFileFilter"> <constructor-arg value=".*.xml"/>
 * </bean> </core-commons:fileFilter> <core-commons:fileList directory="/something"/>
 * </core-commons:fileList>
 *
 * Get all files in the /tmp and /something directory that end in .xml and can be written to
 * <core-commons:fileList directory="/tmp"> <core-commons:fileFilter>
 * <bean class="org.apache.commons.io.filefilter.CanWriteFileFilter"/>
 * <bean class="org.apache.commons.io.filefilter.RegexFileFilter"> <constructor-arg value=".*.xml"/>
 * </bean> </core-commons:fileFilter> <core-commons:fileList directory="/something"/>
 * </core-commons:fileList>
 *
 * Get all files in the /tmp directory and the pom.xml file in another directory
 * <core-commons:fileList> <core-commons:fileList directory="/tmp"/> <value>pom.xml</value>
 * </core-commons:fileList>
 *
 * @author seamans
 *
 */
public class FileListDefinitionParser extends AbstractSingleBeanDefinitionParser {

    /**
     * The bean that is created for this tag element
     * @param element The tag element
     * @return A FileListFactoryBean
     */
    @Override
    protected Class<?> getBeanClass(Element element) {
        return FileListFactoryBean.class;
    }

    /**
     * Called when the fileList tag is to be parsed
     * @param element The tag element
     * @param ctx The context in which the parsing is occuring
     * @param builder The bean definitions build to use
     */
    @Override
    protected void doParse(Element element, ParserContext ctx, BeanDefinitionBuilder builder) {
        // Set the directory property
        builder.addPropertyValue("directory", element.getAttribute("directory"));

        // Set the scope
        builder.setScope(element.getAttribute("scope"));

        // We want any parsing to occur as a child of this tag so we need to make
        // a new one that has this as it's owner/parent
        ParserContext nestedCtx = new ParserContext(ctx.getReaderContext(), ctx.getDelegate(),
                builder.getBeanDefinition());

        // Support for filters
        Element exclusionElem = DomUtils.getChildElementByTagName(element, "fileFilter");
        if (exclusionElem != null) {
            // Just make a new Parser for each one and let the parser do the work
            FileFilterDefinitionParser ff = new FileFilterDefinitionParser();
            builder.addPropertyValue("filters", ff.parse(exclusionElem, nestedCtx));
        }

        // Support for nested fileList
        List<Element> fileLists = DomUtils.getChildElementsByTagName(element, "fileList");
        // Any objects that created will be placed in a ManagedList
        // so Spring does the bulk of the resolution work for us
        ManagedList<Object> nestedFiles = new ManagedList<>();
        if (fileLists.size() > 0) {
            // Just make a new Parser for each one and let them do the work
            FileListDefinitionParser fldp = new FileListDefinitionParser();
            for (Element fileListElem : fileLists) {
                nestedFiles.add(fldp.parse(fileListElem, nestedCtx));
            }
        }

        // Support for other tags that return File (value will be converted to file)
        try {
            // Go through any other tags we may find. This does not mean we support
            // any tag, we support only what parseLimitedList will process
            NodeList nl = element.getChildNodes();
            for (int i = 0; i < nl.getLength(); i++) {
                // Parse each child tag we find in the correct scope but we
                // won't support custom tags at this point as it coudl destablize things
                Object ob = DefinitionParserUtil.parseNode(nl.item(i), ctx, builder.getBeanDefinition(),
                        element.getAttribute("scope"), false);
                if (ob != null) {
                    nestedFiles.add(ob);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // Set the nestedFiles in the properties so it is set on the FactoryBean
        builder.addPropertyValue("nestedFiles", nestedFiles);

    }

}
