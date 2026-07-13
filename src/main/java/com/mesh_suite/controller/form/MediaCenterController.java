package com.mesh_suite.controller.form;

import com.mesh_suite.constant.notify.MediaType;
import com.mesh_suite.constant.forms.Timeline;
import com.mesh_suite.domain.notify.MediaCenter;
import com.mesh_suite.dto.MediaCenterDto;
import com.mesh_suite.dto.Paginate;
import com.mesh_suite.service.notify.MediaCenterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/mesh-suite/v1.0/media")
@Tag(name = "Media Center")
public class MediaCenterController {

    @Autowired
    private MediaCenterService mediaCenterService;

    @Operation(summary = "Create a new media entry")
    @PostMapping(consumes = {"multipart/form-data"})
    public MediaCenter createMedia(@ModelAttribute MediaCenterDto mediaCenterDto) {
        return mediaCenterService.createMedia(mediaCenterDto);
    }

    @Operation(summary = "Update an existing media entry")
    @PutMapping
    public MediaCenter updateMedia(@RequestBody MediaCenter mediaCenter) throws FileUploadException {
        return mediaCenterService.updateMedia(mediaCenter);
    }

    @Operation(summary = "Retrieve a media entry by ID")
    @GetMapping(value = "/{id}")
    public ResponseEntity<MediaCenter> getMediaById(@PathVariable Long id) {
        return ResponseEntity.ok(mediaCenterService.getMediaById(id));
    }

    @Operation(summary = "Delete a media entry by ID")
    @DeleteMapping(value = "/{id}")
    public void deleteMedia(@PathVariable Long id) {
        mediaCenterService.deleteMedia(id);
    }

    @Operation(summary = "Change the is active status")
    @PutMapping("/status/{id}/{isActive}")
    public ResponseEntity<MediaCenter> updateDisplayStatus(
            @PathVariable(name = "id") Long id,
            @PathVariable(name = "isActive") Boolean isActive) {
        MediaCenter updatedMedia = mediaCenterService.updateDisplayStatus(id, isActive);
        return ResponseEntity.ok(updatedMedia);
    }

    @Operation(summary = "Retrieve all media center data")
    @GetMapping("/all/{page}/{size}")
    public ResponseEntity<Paginate<MediaCenter>> getAllMediaCenters(
            @PathVariable int page,
            @PathVariable int size) {
        Paginate<MediaCenter> mediaPage = mediaCenterService.getAllMediaCenters(page, size);
        return ResponseEntity.ok(mediaPage);
    }

    @GetMapping("/filter-media/{mediaType}/{page}/{size}")
    public ResponseEntity<Paginate<MediaCenter>> getMediaCentersByType(
            @PathVariable MediaType mediaType,
            @PathVariable int page,
            @PathVariable int size) {
        Paginate<MediaCenter> mediaPage = mediaCenterService.getMediaCentersByType(mediaType, page, size);
        return ResponseEntity.ok(mediaPage);
    }

    @Operation(summary = "Retrieve all media specific data with status(isActive)")
    @GetMapping("/by-status/{isActive}/{page}/{size}")
    public Paginate<MediaCenter> getMediaByStatus(
            @PathVariable Boolean isActive,
            @PathVariable int page,
            @PathVariable int size) {
        return mediaCenterService.getMediaByStatus(isActive, page, size);
    }

    @Operation(summary = "Search media by heading and media type")
    @GetMapping("/search/{heading}/{mediaType}")
    public List<MediaCenter> searchByHeadingAndType(
            @PathVariable String heading,
            @PathVariable MediaType mediaType) {
        return mediaCenterService.searchMediaByHeadingAndType(heading, mediaType);
    }

    @Operation(summary = "Search media by heading")
    @GetMapping("/search/{heading}")
    public List<MediaCenter> searchByHeading(
            @PathVariable String heading) {
        return mediaCenterService.searchMediaByHeading(heading);
    }

    @Operation(summary = "Retrieve media content by type and time range")
    @GetMapping("/findBy-range/{mediaType}/{timeline}/{page}/{size}")
    public Paginate<MediaCenter> findByMediaTypeAndTimeline(
            @PathVariable MediaType mediaType,
            @PathVariable Timeline timeline,
            @PathVariable int page,
            @PathVariable int size) {
        return mediaCenterService.findByMediaTypeAndTimeline(mediaType, timeline, page, size);
    }
}
