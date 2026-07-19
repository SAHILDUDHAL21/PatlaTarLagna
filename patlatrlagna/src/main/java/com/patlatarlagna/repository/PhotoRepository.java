package com.patlatarlagna.repository;

import com.patlatarlagna.entity.Photo;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PhotoRepository extends JpaRepository<Photo, Long> {
    List<Photo> findByProfileId(Long profileId);
}
