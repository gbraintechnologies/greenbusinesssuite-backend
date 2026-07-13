package com.mesh_suite.service.notify;

import com.mesh_suite.constant.notify.MediaType;
import com.mesh_suite.constant.forms.Timeline;
import com.mesh_suite.dao.notify.MediaCenterRepository;
import com.mesh_suite.domain.notify.MediaCenter;
import com.mesh_suite.dto.MediaCenterDto;
import com.mesh_suite.dto.Paginate;
import com.mesh_suite.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@Service
@Transactional
public class MediaCenterService {
    @Autowired
    private MediaCenterRepository mediaCenterRepository;
    @Autowired
    private S3Service s3Service;

    public MediaCenter createMedia(MediaCenterDto mediaCenterDto) {
        MediaCenter mediaCenter = new MediaCenter();
        mediaCenter.setMediaType(mediaCenterDto.getMediaType());
        mediaCenter.setAltText(mediaCenterDto.getAltText());
        mediaCenter.setHeading(mediaCenterDto.getHeading());
        mediaCenter.setIsActive(mediaCenterDto.getIsActive());
        mediaCenter.setUrl(mediaCenterDto.getUrl());

        // Upload thumbnail to S3 and set the URL
        if (mediaCenterDto.getThumbnail() != null && !mediaCenterDto.getThumbnail().isEmpty()) {
            String thumbnailUrl = s3Service.uploadFile(mediaCenterDto.getThumbnail());
            mediaCenter.setThumbnail(thumbnailUrl);
        }

        return mediaCenterRepository.save(mediaCenter);
    }

    public MediaCenter updateMedia(MediaCenter mediaCenter) {
        return mediaCenterRepository.findById(mediaCenter.getId())
                .map(existingMedia -> {
                    // Update other fields
                    existingMedia.setMediaType(mediaCenter.getMediaType());
                    existingMedia.setAltText(mediaCenter.getAltText());
                    existingMedia.setHeading(mediaCenter.getHeading());
                    existingMedia.setIsActive(mediaCenter.getIsActive());
                    existingMedia.setUrl(mediaCenter.getUrl());
                    existingMedia.setThumbnail(mediaCenter.getThumbnail());
                    return mediaCenterRepository.save(existingMedia);
                })
                .orElseThrow(() -> new ResourceNotFoundException("Media item not found with id " + mediaCenter.getId()));
    }

    public MediaCenter getMediaById(Long id) {
        return mediaCenterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Media item not found with id " + id));
    }

    public void deleteMedia(Long id) {
        if (!mediaCenterRepository.existsById(id)) {
            throw new ResourceNotFoundException("Media item not found to be deleted with id " + id);
        }
        mediaCenterRepository.deleteById(id);
    }

    public MediaCenter updateDisplayStatus(Long id, Boolean isActive) {
        return mediaCenterRepository.findById(id)
                .map(mediaCenter -> {
                    mediaCenter.setIsActive(isActive);
                    return mediaCenterRepository.save(mediaCenter);
                })
                .orElseThrow(() -> new ResourceNotFoundException("MediaCenter not found with id " + id));
    }

    public Paginate<MediaCenter> getAllMediaCenters(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<MediaCenter> mediaPage = mediaCenterRepository.findAll(pageable);

        return new Paginate<>(
                mediaPage.getNumber(),
                mediaPage.getSize(),
                mediaPage.getTotalElements(),
                mediaPage.getTotalPages(),
                mediaPage.getContent()
        );
    }

    public Paginate<MediaCenter> getMediaCentersByType(MediaType mediaType, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<MediaCenter> mediaPage = mediaCenterRepository.findByMediaType(mediaType, pageable);

        return new Paginate<>(
                mediaPage.getNumber(),
                mediaPage.getSize(),
                mediaPage.getTotalElements(),
                mediaPage.getTotalPages(),
                mediaPage.getContent()
        );
    }

    public Paginate<MediaCenter> getMediaByStatus(Boolean isActive, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<MediaCenter> mediaPage = mediaCenterRepository.findAllByIsActive(isActive, pageRequest);

        return new Paginate<>(
                mediaPage.getNumber(),
                mediaPage.getSize(),
                mediaPage.getTotalElements(),
                mediaPage.getTotalPages(),
                mediaPage.getContent()
        );
    }

    public List<MediaCenter> searchMediaByHeadingAndType(String heading, MediaType mediaType) {
        return mediaCenterRepository.findByHeadingContainingIgnoreCaseAndMediaType(heading, mediaType);
    }

    public List<MediaCenter> searchMediaByHeading(String heading) {
        return mediaCenterRepository.findByHeadingContainingIgnoreCase(heading);
    }

    public Paginate<MediaCenter> findByMediaTypeAndTimeline(MediaType mediaType, Timeline timeline, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        Page<MediaCenter> pageResult = (timeline == Timeline.ALL)
                ? mediaCenterRepository.findByMediaType(mediaType, pageable)
                : getMediaCenterByTimeline(mediaType, timeline, pageable);

        return new Paginate<>(
                pageResult.getNumber(),
                pageResult.getSize(),
                pageResult.getTotalElements(),
                pageResult.getTotalPages(),
                pageResult.getContent()
        );
    }

    private Page<MediaCenter> getMediaCenterByTimeline(MediaType mediaType, Timeline timeline, Pageable pageable) {
        LocalDateTime[] dateRange = calculateDateRange(timeline);
        return mediaCenterRepository.findByMediaTypeAndDateRange(mediaType, dateRange[0], dateRange[1], pageable);
    }

    private LocalDateTime[] calculateDateRange(Timeline timeline) {
        LocalDateTime startDate = null;
        LocalDateTime endDate = null;

        switch (timeline) {
            case TODAY:
                startDate = LocalDate.now().atStartOfDay();  // Start of today
                endDate = LocalDate.now().plusDays(1).atStartOfDay().minusNanos(1);  // End of today
                break;
            case THIS_WEEK:
                startDate = LocalDate.now().with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY)).atStartOfDay();
                endDate = startDate.plusWeeks(1).minusNanos(1);
                break;
            case THIS_MONTH:
                startDate = LocalDate.now().with(TemporalAdjusters.firstDayOfMonth()).atStartOfDay();
                endDate = startDate.plusMonths(1).minusNanos(1);
                break;
            case THIS_YEAR:
                startDate = LocalDate.now().with(TemporalAdjusters.firstDayOfYear()).atStartOfDay();
                endDate = startDate.plusYears(1).minusNanos(1);
                break;
        }

        return new LocalDateTime[]{startDate, endDate};
    }


}
