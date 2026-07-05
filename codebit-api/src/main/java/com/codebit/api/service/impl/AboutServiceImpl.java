package com.codebit.api.service.impl;


import com.codebit.api.dto.aboutDto.*;
import com.codebit.api.dto.footerDto.FootprintDto;
import com.codebit.api.dto.footerDto.FootprintPublicDto;
import com.codebit.api.dto.likeDto.LikeResponse;
import com.codebit.api.dto.skillDto.SkillDto;
import com.codebit.api.dto.skillDto.SkillPublicDto;
import com.codebit.api.dto.socialLinkDto.SocialLinkDto;
import com.codebit.api.dto.socialLinkDto.SocialLinkPublicDto;
import com.codebit.api.entity.*;
import com.codebit.api.repository.*;
import com.codebit.api.tools.MarkdownRenderer;
import com.codebit.api.utils.TimeUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class AboutServiceImpl {

    private final AboutRepository aboutRepository;
    private final SocialLinkRepository socialLinkRepository;
    private final SkillRepository skillRepository;
    private final FootprintRepository footprintRepository;
    private final AboutHistoryRepository aboutHistoryRepository;
    private final MarkdownRenderer markdownRenderer;
    private final AboutCacheServiceImpl cacheService;
    private final ObjectMapper objectMapper;


    @Transactional(readOnly = true)
    public AboutResponse getAbout() {
        AboutResponse cached = cacheService.getAboutFromCache();
        if (cached != null) {
            return cached;
        }

        AboutPage about = aboutRepository.findFirstByStatusOrderByUpdatedAtDesc(1)
                .orElseGet(this::createDefaultAbout);

        AboutResponse response = buildAboutResponse(about);
        cacheService.putAboutToCache(response);
        return response;
    }

    @Transactional
    public void incrementViewCount() {
        aboutRepository.findFirstByStatusOrderByUpdatedAtDesc(1)
                .ifPresent(about -> {
                    aboutRepository.incrementViewCount(about.getId());
                    cacheService.evictAboutCache();
                    log.debug("增加浏览量: {}", about.getId());
                });
    }

    @Transactional
    public LikeResponse incrementLikeCount() {
        AboutPage about = aboutRepository.findFirstByStatusOrderByUpdatedAtDesc(1)
                .orElseThrow(() -> new RuntimeException("关于页面不存在"));

        aboutRepository.incrementLikeCount(about.getId());
        cacheService.evictAboutCache();

        return LikeResponse.builder()
                .likeCount(about.getLikeCount() + 1)
                .success(true)
                .message("点赞成功")
                .build();
    }

    // ==================== 管理端接口 ====================

    @Transactional(readOnly = true)
    public AboutAdminResponse getAboutForAdmin() {
        AboutPage about = aboutRepository.findFirstByStatusOrderByUpdatedAtDesc(1)
                .orElse(new AboutPage());

        return AboutAdminResponse.builder()
                .title(about.getTitle())
                .subtitle(about.getSubtitle())
                .content(about.getContent())
                .avatarUrl(about.getAvatarUrl())
                .coverImageUrl(about.getCoverImageUrl())
                .email(about.getEmail())
                .location(about.getLocation())
                .status(about.getStatus())
                .socialLinks(convertToSocialLinkDtoList(about.getSocialLinks()))
                .skills(convertToSkillDtoList(about.getSkills()))
                .footprints(convertToFootprintDtoList(about.getFootprints()))
                .version(about.getVersion())
                .updatedAt(about.getUpdatedAt())
                .updatedBy(about.getUpdatedBy())
                .build();
    }

    @Transactional
    public void updateAbout(AboutUpdateRequest request, String adminUser) {
        AboutPage about = aboutRepository.findFirstByStatusOrderByUpdatedAtDesc(1)
                .orElse(new AboutPage());

        // 保存历史版本
        saveHistorySnapshot(about, adminUser, request.getChangeReason());

        // 更新基本信息
        about.setTitle(request.getTitle());
        about.setSubtitle(request.getSubtitle());
        about.setContent(request.getContent());
        about.setContentHtml(markdownRenderer.render(request.getContent()));
        about.setAvatarUrl(request.getAvatarUrl());
        about.setCoverImageUrl(request.getCoverImageUrl());
        about.setEmail(request.getEmail());
        about.setLocation(request.getLocation());
        about.setStatus(request.getStatus() != null ? request.getStatus() : 1);
        about.setUpdatedBy(adminUser);

        AboutPage saved = aboutRepository.save(about);

        // 更新关联数据
        updateRelatedData(saved, request);

        log.info("关于页面已更新，更新人: {}", adminUser);
        cacheService.evictAboutCache();
    }

    @Transactional(readOnly = true)
    public List<AboutHistoryResponse> getVersionHistory() {
        AboutPage about = aboutRepository.findFirstByStatusOrderByUpdatedAtDesc(1)
                .orElseThrow(() -> new RuntimeException("关于页面不存在"));

        return aboutHistoryRepository.findByAboutIdOrderByVersionDesc(about.getId())
                .stream()
                .map(this::convertToHistoryResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void rollbackToVersion(Integer version, String adminUser) {
        AboutPage about = aboutRepository.findFirstByStatusOrderByUpdatedAtDesc(1)
                .orElseThrow(() -> new RuntimeException("关于页面不存在"));

        AboutHistory history = aboutHistoryRepository.findByAboutIdAndVersion(about.getId(), version)
                .orElseThrow(() -> new RuntimeException("版本 " + version + " 不存在"));

        // 保存当前版本
        saveHistorySnapshot(about, adminUser, "回滚到版本 " + version + " 前的备份");

        // 恢复数据
        about.setContent(history.getContent());
        about.setContentHtml(history.getContentHtml());

        restoreSocialLinksFromJson(about, history.getSocialLinksSnapshot());
        restoreSkillsFromJson(about, history.getSkillsSnapshot());
        restoreFootprintsFromJson(about, history.getFootprintsSnapshot());

        about.setUpdatedBy(adminUser);
        aboutRepository.save(about);

        log.info("已回滚到版本 {}，操作人: {}", version, adminUser);
        cacheService.evictAboutCache();
    }

    @Transactional(readOnly = true)
    public AboutStatisticsResponse getStatistics() {
        AboutPage about = aboutRepository.findFirstByStatusOrderByUpdatedAtDesc(1)
                .orElseThrow(() -> new RuntimeException("关于页面不存在"));

        Long totalVersions = (long) aboutHistoryRepository.findByAboutIdOrderByVersionDesc(about.getId()).size();

        Map<Integer, Long> footprintStats = new HashMap<>();
        List<Object[]> footprintResults = footprintRepository.countByYear(about.getId());
        for (Object[] row : footprintResults) {
            footprintStats.put((Integer) row[0], (Long) row[1]);
        }

        Map<String, Long> skillStats = new HashMap<>();
        List<Object[]> skillResults = skillRepository.countByCategory(about.getId());
        for (Object[] row : skillResults) {
            String category = (String) row[0];
            Long count = (Long) row[1];
            skillStats.put(category != null ? category : "其他", count);
        }

        return AboutStatisticsResponse.builder()
                .totalViewCount(about.getViewCount())
                .totalLikeCount(about.getLikeCount())
                .currentVersion(about.getVersion())
                .totalVersions(totalVersions.intValue())
                .footprintYearStats(footprintStats)
                .skillCategoryStats(skillStats)
                .lastUpdated(about.getUpdatedAt())
                .build();
    }

    // ==================== 私有辅助方法 ====================

    private AboutPage createDefaultAbout() {
        AboutPage about = new AboutPage();
        about.setTitle("关于我");
        about.setContent("# 关于我\n\n欢迎来到我的博客！");
        about.setContentHtml("<h1>关于我</h1><p>欢迎来到我的博客！</p>");
        about.setStatus(1);
        return aboutRepository.save(about);
    }

    private AboutResponse buildAboutResponse(AboutPage about) {
        return AboutResponse.builder()
                .title(about.getTitle())
                .subtitle(about.getSubtitle())
                .contentHtml(about.getContentHtml())
                .avatarUrl(about.getAvatarUrl())
                .coverImageUrl(about.getCoverImageUrl())
                .email(about.getEmail())
                .location(about.getLocation())
                .socialLinks(convertToPublicSocialLinkDtoList(about.getSocialLinks()))
                .skills(convertToPublicSkillDtoList(about.getSkills()))
                .footprints(convertToPublicFootprintDtoList(about.getFootprints()))
                .viewCount(about.getViewCount())
                .likeCount(about.getLikeCount())
                .buildDays(TimeUtil.calculateBuildDays())
                .lastUpdated(TimeUtil.formatRelativeTime(about.getUpdatedAt()))
                .build();
    }

    private void updateRelatedData(AboutPage about, AboutUpdateRequest request) {
        if (request.getSocialLinks() != null) {
            socialLinkRepository.deleteByAboutId(about.getId());
            List<SocialLink> newLinks = request.getSocialLinks().stream()
                    .map(dto -> createSocialLink(about, dto))
                    .collect(Collectors.toList());
            socialLinkRepository.saveAll(newLinks);
        }

        if (request.getSkills() != null) {
            skillRepository.deleteByAboutId(about.getId());
            List<Skill> newSkills = request.getSkills().stream()
                    .map(dto -> createSkill(about, dto))
                    .collect(Collectors.toList());
            skillRepository.saveAll(newSkills);
        }

        if (request.getFootprints() != null) {
            footprintRepository.deleteByAboutId(about.getId());
            List<Footprint> newFootprints = request.getFootprints().stream()
                    .map(dto -> createFootprint(about, dto))
                    .collect(Collectors.toList());
            footprintRepository.saveAll(newFootprints);
        }
    }

    private void saveHistorySnapshot(AboutPage about, String adminUser, String changeReason) {
        if (about.getId() == null) return;

        AboutHistory history = AboutHistory.builder()
                .aboutId(about.getId())
                .content(about.getContent())
                .contentHtml(about.getContentHtml())
                .version(about.getVersion() + 1)
                .socialLinksSnapshot(exportSocialLinksToJson(about.getSocialLinks()))
                .skillsSnapshot(exportSkillsToJson(about.getSkills()))
                .footprintsSnapshot(exportFootprintsToJson(about.getFootprints()))
                .changedBy(adminUser)
                .changeReason(changeReason)
                .build();

        aboutHistoryRepository.save(history);
        aboutHistoryRepository.cleanOldVersions(about.getId(), 20);
    }

    // ==================== JSON 导出方法 ====================

    private String exportSocialLinksToJson(List<SocialLink> links) {
        if (links == null || links.isEmpty()) return "[]";

        List<Map<String, Object>> list = links.stream().map(link -> {
            Map<String, Object> map = new HashMap<>();
            map.put("platform", link.getPlatform());
            map.put("displayName", link.getDisplayName());
            map.put("url", link.getUrl());
            map.put("iconClass", link.getIconClass());
            map.put("sortOrder", link.getSortOrder());
            return map;
        }).collect(Collectors.toList());

        try {
            return objectMapper.writeValueAsString(list);
        } catch (Exception e) {
            log.error("导出社交链接JSON失败", e);
            return "[]";
        }
    }

    private String exportSkillsToJson(List<Skill> skills) {
        if (skills == null || skills.isEmpty()) return "[]";

        List<Map<String, Object>> list = skills.stream().map(skill -> {
            Map<String, Object> map = new HashMap<>();
            map.put("name", skill.getName());
            map.put("category", skill.getCategory());
            map.put("proficiency", skill.getProficiency());
            map.put("yearsOfExperience", skill.getYearsOfExperience());
            map.put("sortOrder", skill.getSortOrder());
            return map;
        }).collect(Collectors.toList());

        try {
            return objectMapper.writeValueAsString(list);
        } catch (Exception e) {
            log.error("导出技能JSON失败", e);
            return "[]";
        }
    }

    private String exportFootprintsToJson(List<Footprint> footprints) {
        if (footprints == null || footprints.isEmpty()) return "[]";

        List<Map<String, Object>> list = footprints.stream().map(fp -> {
            Map<String, Object> map = new HashMap<>();
            map.put("year", fp.getYear());
            map.put("month", fp.getMonth());
            map.put("title", fp.getTitle());
            map.put("description", fp.getDescription());
            map.put("location", fp.getLocation());
            map.put("isHighlight", fp.getIsHighlight());
            map.put("sortOrder", fp.getSortOrder());
            return map;
        }).collect(Collectors.toList());

        try {
            return objectMapper.writeValueAsString(list);
        } catch (Exception e) {
            log.error("导出足迹JSON失败", e);
            return "[]";
        }
    }

    // ==================== JSON 恢复方法 ====================

    private void restoreSocialLinksFromJson(AboutPage about, String json) {
        if (json == null || json.trim().isEmpty() || "[]".equals(json)) return;

        try {
            socialLinkRepository.deleteByAboutId(about.getId());
            List<Map<String, Object>> links = objectMapper.readValue(json, List.class);
            List<SocialLink> socialLinks = new ArrayList<>();

            for (Map<String, Object> linkMap : links) {
                SocialLink link = new SocialLink();
                link.setAbout(about);
                link.setPlatform((String) linkMap.get("platform"));
                link.setDisplayName((String) linkMap.get("displayName"));
                link.setUrl((String) linkMap.get("url"));
                link.setIconClass((String) linkMap.get("iconClass"));
                link.setSortOrder((Integer) linkMap.getOrDefault("sortOrder", 0));
                link.setEnabled(1);
                socialLinks.add(link);
            }
            socialLinkRepository.saveAll(socialLinks);
        } catch (Exception e) {
            log.error("恢复社交链接失败", e);
        }
    }

    private void restoreSkillsFromJson(AboutPage about, String json) {
        if (json == null || json.trim().isEmpty() || "[]".equals(json)) return;

        try {
            skillRepository.deleteByAboutId(about.getId());
            List<Map<String, Object>> skillsList = objectMapper.readValue(json, List.class);
            List<Skill> skills = new ArrayList<>();

            for (Map<String, Object> skillMap : skillsList) {
                Skill skill = new Skill();
                skill.setAbout(about);
                skill.setName((String) skillMap.get("name"));
                skill.setCategory((String) skillMap.get("category"));
                skill.setProficiency((Integer) skillMap.getOrDefault("proficiency", 70));
                if (skillMap.get("yearsOfExperience") != null) {
                    skill.setYearsOfExperience(new BigDecimal(skillMap.get("yearsOfExperience").toString()));
                }
                skill.setSortOrder((Integer) skillMap.getOrDefault("sortOrder", 0));
                skills.add(skill);
            }
            skillRepository.saveAll(skills);
        } catch (Exception e) {
            log.error("恢复技能失败", e);
        }
    }

    private void restoreFootprintsFromJson(AboutPage about, String json) {
        if (json == null || json.trim().isEmpty() || "[]".equals(json)) return;

        try {
            footprintRepository.deleteByAboutId(about.getId());
            List<Map<String, Object>> footprintsList = objectMapper.readValue(json, List.class);
            List<Footprint> footprints = new ArrayList<>();

            for (Map<String, Object> fpMap : footprintsList) {
                Footprint fp = new Footprint();
                fp.setAbout(about);
                fp.setYear((Integer) fpMap.get("year"));
                fp.setMonth((Integer) fpMap.get("month"));
                fp.setTitle((String) fpMap.get("title"));
                fp.setDescription((String) fpMap.get("description"));
                fp.setLocation((String) fpMap.get("location"));
                fp.setIsHighlight((Integer) fpMap.getOrDefault("isHighlight", 0));
                fp.setSortOrder((Integer) fpMap.getOrDefault("sortOrder", 0));
                footprints.add(fp);
            }
            footprintRepository.saveAll(footprints);
        } catch (Exception e) {
            log.error("恢复足迹失败", e);
        }
    }

    // ==================== 创建实体方法 ====================

    private SocialLink createSocialLink(AboutPage about, SocialLinkDto dto) {
        SocialLink link = new SocialLink();
        link.setAbout(about);
        link.setPlatform(dto.getPlatform());
        link.setDisplayName(dto.getDisplayName());
        link.setUrl(dto.getUrl());
        link.setIconClass(dto.getIconClass());
        link.setColor(dto.getColor());
        link.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0);
        link.setEnabled(dto.getEnabled() != null && dto.getEnabled() ? 1 : 0);
        return link;
    }

    private Skill createSkill(AboutPage about, SkillDto dto) {
        Skill skill = new Skill();
        skill.setAbout(about);
        skill.setName(dto.getName());
        skill.setCategory(dto.getCategory());
        skill.setProficiency(dto.getProficiency());
        skill.setYearsOfExperience(dto.getYearsOfExperience());
        skill.setIcon(dto.getIcon());
        skill.setColor(dto.getColor());
        skill.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0);
        return skill;
    }

    private Footprint createFootprint(AboutPage about, FootprintDto dto) {
        Footprint footprint = new Footprint();
        footprint.setAbout(about);
        footprint.setYear(dto.getYear());
        footprint.setMonth(dto.getMonth());
        footprint.setTitle(dto.getTitle());
        footprint.setDescription(dto.getDescription());
        footprint.setLocation(dto.getLocation());
        footprint.setIcon(dto.getIcon());
        footprint.setImageUrl(dto.getImageUrl());
        footprint.setIsHighlight(Boolean.TRUE.equals(dto.getIsHighlight()) ? 1 : 0);
        footprint.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0);
        footprint.setLinkUrl(dto.getLinkUrl());
        footprint.setLinkText(dto.getLinkText());
        return footprint;
    }

    // ==================== 转换方法 ====================

    private List<SocialLinkPublicDto> convertToPublicSocialLinkDtoList(List<SocialLink> links) {
        if (links == null) return Collections.emptyList();
        return links.stream()
                .filter(link -> link.getEnabled() == 1)
                .sorted(Comparator.comparing(SocialLink::getSortOrder))
                .map(link -> SocialLinkPublicDto.builder()
                        .platform(link.getPlatform())
                        .displayName(link.getDisplayName())
                        .url(link.getUrl())
                        .iconClass(link.getIconClass())
                        .color(link.getColor())
                        .build())
                .collect(Collectors.toList());
    }

    private List<SocialLinkDto> convertToSocialLinkDtoList(List<SocialLink> links) {
        if (links == null) return Collections.emptyList();
        return links.stream()
                .map(link -> SocialLinkDto.builder()
                        .id(link.getId())
                        .platform(link.getPlatform())
                        .displayName(link.getDisplayName())
                        .url(link.getUrl())
                        .iconClass(link.getIconClass())
                        .color(link.getColor())
                        .sortOrder(link.getSortOrder())
                        .enabled(link.getEnabled() == 1)
                        .build())
                .collect(Collectors.toList());
    }

    private List<SkillPublicDto> convertToPublicSkillDtoList(List<Skill> skills) {
        if (skills == null) return Collections.emptyList();
        return skills.stream()
                .sorted(Comparator.comparing(Skill::getCategory)
                        .thenComparing(Skill::getSortOrder))
                .map(skill -> SkillPublicDto.builder()
                        .name(skill.getName())
                        .category(skill.getCategory())
                        .proficiency(skill.getProficiency())
                        .yearsOfExperience(skill.getYearsOfExperience())
                        .icon(skill.getIcon())
                        .color(skill.getColor())
                        .build())
                .collect(Collectors.toList());
    }

    private List<SkillDto> convertToSkillDtoList(List<Skill> skills) {
        if (skills == null) return Collections.emptyList();
        return skills.stream()
                .map(skill -> SkillDto.builder()
                        .id(skill.getId())
                        .name(skill.getName())
                        .category(skill.getCategory())
                        .proficiency(skill.getProficiency())
                        .yearsOfExperience(skill.getYearsOfExperience())
                        .icon(skill.getIcon())
                        .color(skill.getColor())
                        .sortOrder(skill.getSortOrder())
                        .build())
                .collect(Collectors.toList());
    }

    private List<FootprintPublicDto> convertToPublicFootprintDtoList(List<Footprint> footprints) {
        if (footprints == null) return Collections.emptyList();
        return footprints.stream()
                .sorted(Comparator.comparing(Footprint::getYear).reversed()
                        .thenComparing(Footprint::getSortOrder))
                .map(fp -> FootprintPublicDto.builder()
                        .id(fp.getId())
                        .year(fp.getYear())
                        .month(fp.getMonth())
                        .title(fp.getTitle())
                        .description(fp.getDescription())
                        .location(fp.getLocation())
                        .icon(fp.getIcon())
                        .imageUrl(fp.getImageUrl())
                        .isHighlight(fp.getIsHighlight() == 1)
                        .linkUrl(fp.getLinkUrl())
                        .linkText(fp.getLinkText())
                        .formattedDate(TimeUtil.formatFootprintDate(fp.getYear(), fp.getMonth()))
                        .build())
                .collect(Collectors.toList());
    }

    private List<FootprintDto> convertToFootprintDtoList(List<Footprint> footprints) {
        if (footprints == null) return Collections.emptyList();
        return footprints.stream()
                .map(fp -> FootprintDto.builder()
                        .id(fp.getId())
                        .year(fp.getYear())
                        .month(fp.getMonth())
                        .title(fp.getTitle())
                        .description(fp.getDescription())
                        .location(fp.getLocation())
                        .icon(fp.getIcon())
                        .imageUrl(fp.getImageUrl())
                        .isHighlight(fp.getIsHighlight() == 1)
                        .sortOrder(fp.getSortOrder())
                        .linkUrl(fp.getLinkUrl())
                        .linkText(fp.getLinkText())
                        .build())
                .collect(Collectors.toList());
    }

    private AboutHistoryResponse convertToHistoryResponse(AboutHistory history) {
        if (history == null) return null;
        return AboutHistoryResponse.builder()
                .id(history.getId())
                .version(history.getVersion())
                .content(history.getContent())
                .contentHtml(history.getContentHtml())
                .socialLinksSnapshot(history.getSocialLinksSnapshot())
                .skillsSnapshot(history.getSkillsSnapshot())
                .footprintsSnapshot(history.getFootprintsSnapshot())
                .changedBy(history.getChangedBy())
                .changeReason(history.getChangeReason())
                .createdAt(history.getCreatedAt())
                .formattedCreatedAt(TimeUtil.formatDateTime(history.getCreatedAt()))
                .build();
    }
}