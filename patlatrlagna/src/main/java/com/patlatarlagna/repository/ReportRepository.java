package com.patlatarlagna.repository;

import com.patlatarlagna.entity.Report;
import com.patlatarlagna.enums.ReportStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReportRepository extends JpaRepository<Report, Long> {
    List<Report> findByStatus(ReportStatus status);
}
