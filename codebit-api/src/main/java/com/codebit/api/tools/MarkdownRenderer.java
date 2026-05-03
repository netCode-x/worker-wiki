package com.codebit.api.tools;

import org.commonmark.Extension;
import org.commonmark.ext.autolink.AutolinkExtension;
import org.commonmark.ext.gfm.strikethrough.StrikethroughExtension;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.ext.heading.anchor.HeadingAnchorExtension;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class MarkdownRenderer {

    private final Parser parser;
    private final HtmlRenderer renderer;

    public MarkdownRenderer() {
        List<Extension> extensions = Arrays.asList(
                TablesExtension.create(),
                StrikethroughExtension.create(),
                AutolinkExtension.create(),
                HeadingAnchorExtension.create()
        );

        this.parser = Parser.builder()
                .extensions(extensions)
                .build();

        this.renderer = HtmlRenderer.builder()
                .extensions(extensions)
                .escapeHtml(true)
                .sanitizeUrls(true)
                .build();
    }

    public String render(String markdown) {
        if (markdown == null || markdown.trim().isEmpty()) {
            return "";
        }

        try {
            Node document = parser.parse(markdown);
            return renderer.render(document);
        } catch (Exception e) {
            return "<p>Markdown渲染失败: " + e.getMessage() + "</p>" + markdown;
        }
    }

    public String renderSummary(String markdown, int maxLength) {
        String html = render(markdown);
        String plainText = html.replaceAll("<[^>]*>", "");
        if (plainText.length() > maxLength) {
            return plainText.substring(0, maxLength) + "...";
        }
        return plainText;
    }
}