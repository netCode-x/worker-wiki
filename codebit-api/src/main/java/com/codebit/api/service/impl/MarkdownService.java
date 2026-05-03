package com.codebit.api.service.impl;

import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.data.MutableDataSet;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.ext.gfm.strikethrough.StrikethroughExtension;
import com.vladsch.flexmark.ext.autolink.AutolinkExtension;
import org.springframework.stereotype.Service;
import java.util.Arrays;

@Service
public class MarkdownService {

    private final Parser parser;
    private final HtmlRenderer renderer;

    public MarkdownService() {
        MutableDataSet options = new MutableDataSet();

        // 启用扩展（表格、删除线、自动链接）
        options.set(Parser.EXTENSIONS, Arrays.asList(
                TablesExtension.create(),          // 表格支持
                StrikethroughExtension.create(),   // 删除线 ~~text~~
                AutolinkExtension.create()         // 自动链接
        ));

        // 安全设置：转义 HTML 标签防止 XSS 攻击
        options.set(HtmlRenderer.ESCAPE_HTML, true);

        this.parser = Parser.builder(options).build();
        this.renderer = HtmlRenderer.builder(options).build();
    }

    /**
     * 将 Markdown 转换为 HTML
     * @param markdown 原始 Markdown 内容
     * @return 转换后的 HTML 字符串
     */
    public String toHtml(String markdown) {
        if (markdown == null || markdown.trim().isEmpty()) {
            return "";
        }
        com.vladsch.flexmark.util.ast.Node document = parser.parse(markdown);
        return renderer.render(document);
    }

    /**
     * 将 Markdown 转换为纯文本（用于生成文章简介）
     * @param markdown 原始 Markdown 内容
     * @param maxLength 最大长度
     * @return 纯文本简介
     */
    public String toPlainText(String markdown, int maxLength) {
        String html = toHtml(markdown);
        // 去除 HTML 标签，只保留文本
        String text = html.replaceAll("<[^>]+>", "")
                .replaceAll("&[a-zA-Z]+;", " ")
                .replaceAll("\\s+", " ")
                .trim();
        if (text.length() > maxLength) {
            text = text.substring(0, maxLength) + "...";
        }
        return text;
    }
}