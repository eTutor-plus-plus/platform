package at.jku.dke.etutor.helper;
import org.apache.jena.atlas.lib.NotImplemented;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.context.Context;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.xhtmlrenderer.pdf.ITextRenderer;


import java.io.*;

/* Idea is the following:
 * -----------------------
 * Use an already existing template engine (here thymeleaf) to generate a html or latex file
 * and convert it to pdf
 */

public class PdfGenerator {
    private final Logger log = LoggerFactory.getLogger(PdfGenerator.class);

    public byte[] getPdf(String template, String studentName){
        String doc =  parseThymeleafTemplate(template, studentName);
        return generatePdfFromHtml(doc);
    }

    private byte[] generatePdfFromLatex(String tex){
        throw new NotImplemented();
    }

    private byte[] generatePdfFromHtml(String html) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        ITextRenderer renderer = new ITextRenderer();
        renderer.setDocumentFromString(html);
        renderer.layout();

        try {
            renderer.createPDF(outputStream);
            //TODO you can also do it with pipes and streams, but the benefit could me marginal
            return outputStream.toByteArray();
        } catch (com.lowagie.text.DocumentException e) {
            log.error("error creating pdf file", e);
        }

        return null;
    }

    private String parseThymeleafTemplate(String template, String name) {
        ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setSuffix(".html");
        templateResolver.setTemplateMode(TemplateMode.HTML);

        TemplateEngine templateEngine = new TemplateEngine();
        templateEngine.setTemplateResolver(templateResolver);

        //TODO setting variables + iteration
        Context context = new Context();
        context.setVariable("name", name);

        return templateEngine.process(template, context);
    }
}
