package com.codebit.api.converter;

import com.codebit.api.dto.ArticleResponse;
import com.codebit.api.dto.NoteResponse;
import com.codebit.api.entity.Article;
import com.codebit.api.entity.ArticleBody;
import com.codebit.api.entity.Note;
import com.codebit.api.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * @Auther: yangkaihu
 * @Date: 2026/4/15 星期三
 * @Description:
 * @VERSON: 17
 */

@Mapper(componentModel = "spring")
public interface BuildResponseConverter {

    /**
     *  文章 构建响应 DTO（详情页使用）
     */
    @Mapping(target = "content", expression = "java(articleBody !=null ? articleBody.getContent() : null)")
    @Mapping(target = "contentHtml",expression = "java(articleBody !=null ? articleBody.getContentHtml() : null)")
    @Mapping(target = "authorName",expression = "java(author !=null ? author.getNickName() : \"未知作者\")")
    @Mapping(target = "id", source = "article.id")
    @Mapping(target = "createDate", source = "article.createDate")
    @Mapping(target = "updateDate", source = "article.updateDate")
    ArticleResponse ArticleConverterResponse(Article article,
                                             ArticleBody articleBody,
                                             User author);


    /**
     * 随记DTO 转换
     * @param note
     * @param author
     * @return
     */
    @Mapping(target = "authorName", expression = "java(author  !=null ?  author.getNickName(): \"未知用户\")")
    @Mapping(target = "updateDate", source = "note.updateDate")
    @Mapping(target = "createDate", source = "note.createDate")
    @Mapping(target = "id", source = "note.id")
    NoteResponse buildResponse(Note note, User author);

}
