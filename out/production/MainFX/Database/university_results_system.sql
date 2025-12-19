-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Dec 19, 2025 at 05:06 PM
-- Server version: 10.4.32-MariaDB
-- PHP Version: 8.0.30

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `university_results_system`
--

-- --------------------------------------------------------

--
-- Table structure for table `courses`
--

CREATE TABLE `courses` (
  `course_id` int(11) NOT NULL,
  `course_code` varchar(20) NOT NULL,
  `course_name` varchar(150) NOT NULL,
  `credits` int(11) NOT NULL,
  `level` varchar(20) DEFAULT NULL,
  `department` varchar(100) DEFAULT NULL,
  `instructor_name` varchar(100) DEFAULT NULL,
  `room` varchar(50) DEFAULT NULL,
  `schedule` varchar(100) DEFAULT NULL,
  `status` varchar(20) DEFAULT 'Active',
  `pdf_path` varchar(500) DEFAULT NULL,
  `folder_name` varchar(200) DEFAULT NULL,
  `description` text DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `courses`
--

INSERT INTO `courses` (`course_id`, `course_code`, `course_name`, `credits`, `level`, `department`, `instructor_name`, `room`, `schedule`, `status`, `pdf_path`, `folder_name`, `description`, `created_at`) VALUES
(1, 'CS301', 'Data Structures', 3, 'Level 3', 'Computer Science', 'Dr. Sarah Johnson', 'CS-205', 'Mon and Wed 10:00-11:30', 'Active', '/materials/ds_syllabus.pdf', 'ds_materials', 'Learn about arrays, linked lists, trees, and graphs.', '2025-12-19 13:10:14'),
(2, 'CS302', 'Algorithms', 3, 'Level 3', 'Computer Science', 'Prof. Michael Chen', 'CS-210', 'Tue and Thu 09:00-10:30', 'Active', '/materials/algorithms.pdf', 'algo_materials', 'Study sorting, searching, and algorithm design.', '2025-12-19 13:10:14'),
(3, 'CS303', 'Database Systems', 3, 'Level 3', 'Computer Science', 'Dr. Emily Rodriguez', 'CS-215', 'Sun and Tue 12:00-13:30', 'Active', '/materials/database.pdf', 'db_materials', 'Introduction to SQL and database design.', '2025-12-19 13:10:14');

-- --------------------------------------------------------

--
-- Table structure for table `enrollments`
--

CREATE TABLE `enrollments` (
  `enrollment_id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `course_id` int(11) NOT NULL,
  `enrollment_date` date DEFAULT curdate(),
  `progress_percentage` double DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `enrollments`
--

INSERT INTO `enrollments` (`enrollment_id`, `user_id`, `course_id`, `enrollment_date`, `progress_percentage`) VALUES
(1, 2, 1, '2025-12-19', 75.5),
(2, 2, 2, '2025-12-19', 60),
(3, 3, 1, '2025-12-19', 90);

-- --------------------------------------------------------

--
-- Table structure for table `lectures`
--

CREATE TABLE `lectures` (
  `lecture_id` int(11) NOT NULL,
  `course_id` int(11) NOT NULL,
  `title` varchar(200) NOT NULL,
  `file_path` varchar(500) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `lectures`
--

INSERT INTO `lectures` (`lecture_id`, `course_id`, `title`, `file_path`, `created_at`) VALUES
(1, 1, 'Lecture 1: Introduction to Data Structures', '/lectures/ds_lecture1.pdf', '2025-12-19 13:10:14'),
(2, 1, 'Lecture 2: Arrays and Linked Lists', '/lectures/ds_lecture2.pdf', '2025-12-19 13:10:14'),
(3, 2, 'Lecture 1: Algorithm Analysis', '/lectures/algo_lecture1.pdf', '2025-12-19 13:10:14');

-- --------------------------------------------------------

--
-- Table structure for table `password_reset_tokens`
--

CREATE TABLE `password_reset_tokens` (
  `token_id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `verification_code` varchar(6) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `expires_at` timestamp NOT NULL DEFAULT (current_timestamp() + interval 15 minute),
  `used` tinyint(1) DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `password_reset_tokens`
--

INSERT INTO `password_reset_tokens` (`token_id`, `user_id`, `verification_code`, `created_at`, `expires_at`, `used`) VALUES
(1, 2, '123456', '2025-12-19 13:10:15', '2025-12-19 13:25:15', 0),
(2, 3, '654321', '2025-12-19 13:10:15', '2025-12-19 13:25:15', 0);

-- --------------------------------------------------------

--
-- Table structure for table `reports`
--

CREATE TABLE `reports` (
  `report_id` int(11) NOT NULL,
  `report_type` varchar(50) NOT NULL,
  `title` varchar(200) NOT NULL,
  `description` text DEFAULT NULL,
  `content` longtext DEFAULT NULL,
  `generated_by` varchar(100) NOT NULL,
  `generation_date` date NOT NULL,
  `level` varchar(20) DEFAULT NULL,
  `department` varchar(100) DEFAULT NULL,
  `file_path` varchar(500) DEFAULT NULL,
  `download_count` int(11) DEFAULT 0,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `reports`
--

INSERT INTO `reports` (`report_id`, `report_type`, `title`, `description`, `content`, `generated_by`, `generation_date`, `level`, `department`, `file_path`, `download_count`, `created_at`) VALUES
(1, 'Academic Report', 'Fall 2024 Semester Report', 'Academic performance report for Fall 2024 semester', 'Report content here', 'Admin', '2024-12-20', 'Level 3', 'Computer Science', NULL, 0, '2025-12-19 13:10:15'),
(2, 'Attendance Report', 'November 2024 Attendance', 'Monthly attendance report for November', 'Attendance details here', 'Admin', '2024-12-01', 'All Levels', 'All Departments', NULL, 0, '2025-12-19 13:10:15'),
(3, 'Financial Report', '2024 Financial Summary', 'Annual financial report for 2024', 'Financial data here', 'Admin', '2024-12-15', 'All Levels', 'All Departments', NULL, 0, '2025-12-19 13:10:15');

-- --------------------------------------------------------

--
-- Table structure for table `results`
--

CREATE TABLE `results` (
  `result_id` int(11) NOT NULL,
  `enrollment_id` int(11) NOT NULL,
  `grade` varchar(5) DEFAULT NULL,
  `gpa_points` double DEFAULT NULL,
  `degree` int(11) DEFAULT NULL,
  `exam_date` date DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `results`
--

INSERT INTO `results` (`result_id`, `enrollment_id`, `grade`, `gpa_points`, `degree`, `exam_date`) VALUES
(1, 1, 'A', 4, 95, '2024-12-15'),
(2, 2, 'B+', 3.5, 85, '2024-12-18'),
(3, 3, 'A-', 3.7, 90, '2024-12-10');

-- --------------------------------------------------------

--
-- Table structure for table `students`
--

CREATE TABLE `students` (
  `student_id` int(11) NOT NULL,
  `student_code` varchar(20) NOT NULL,
  `level` varchar(20) DEFAULT NULL,
  `department` varchar(100) DEFAULT NULL,
  `gpa` double DEFAULT 0,
  `status` varchar(20) DEFAULT 'Active'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `students`
--

INSERT INTO `students` (`student_id`, `student_code`, `level`, `department`, `gpa`, `status`) VALUES
(2, '20240001', 'Level 3', 'Computer Science', 3.68, 'Active'),
(3, '20240002', 'Level 3', 'Computer Science', 3.45, 'Active'),
(4, '20240003', 'Level 2', 'Information Technology', 3.25, 'Active'),
(5, '20240004', 'Level 4', 'Software Engineering', 3.82, 'Active');

-- --------------------------------------------------------

--
-- Table structure for table `student_results`
--

CREATE TABLE `student_results` (
  `result_id` int(11) NOT NULL,
  `student_id` varchar(50) DEFAULT NULL,
  `course_id` int(11) DEFAULT NULL,
  `degree` double DEFAULT NULL,
  `grade` varchar(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `student_results`
--

INSERT INTO `student_results` (`result_id`, `student_id`, `course_id`, `degree`, `grade`) VALUES
(1, '20240001', 1, 95, 'Excellent'),
(2, '20240001', 2, 85, 'Very Good'),
(3, '20240002', 1, 90, 'Excellent');

-- --------------------------------------------------------

--
-- Table structure for table `users`
--

CREATE TABLE `users` (
  `user_id` int(11) NOT NULL,
  `full_name` varchar(100) NOT NULL,
  `username` varchar(50) NOT NULL,
  `email` varchar(100) NOT NULL,
  `password_hash` varchar(255) NOT NULL,
  `role` enum('Student','Admin') DEFAULT 'Student',
  `phone` varchar(20) DEFAULT NULL,
  `profile_image_path` varchar(500) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `users`
--

INSERT INTO `users` (`user_id`, `full_name`, `username`, `email`, `password_hash`, `role`, `phone`, `profile_image_path`, `created_at`) VALUES
(1, 'System Administrator', 'admin', 'admin@university.edu.eg', 'admin123', 'Admin', '+20 100 000 0001', NULL, '2025-12-19 13:10:14'),
(2, 'Ahmed Mohamed', 'ahmed2024', 'ahmed@university.edu.eg', 'student123', 'Student', '+20 111 111 1111', 'C:\\Users\\MOHAMED REDA\\OneDrive\\Pictures\\2666526d6a41385d9a681e1500b0d1e6.jpg', '2025-12-19 13:10:14'),
(3, 'Sarah Johnson', 'sarah2024', 'sarah@university.edu.eg', 'student123', 'Student', '+20 122 222 2222', NULL, '2025-12-19 13:10:14'),
(4, 'Mohamed Ali', 'mohamed2024', 'mohamed@university.edu.eg', 'student123', 'Student', '+20 133 333 3333', NULL, '2025-12-19 13:10:14'),
(5, 'Fatma Hassan', 'fatma2024', 'fatma@university.edu.eg', 'student123', 'Student', '+20 144 444 4444', NULL, '2025-12-19 13:10:14'),
(6, '', '', '@codex.com', '12345678', 'Student', '', NULL, '2025-12-19 13:54:18');

--
-- Indexes for dumped tables
--

--
-- Indexes for table `courses`
--
ALTER TABLE `courses`
  ADD PRIMARY KEY (`course_id`),
  ADD UNIQUE KEY `course_code` (`course_code`),
  ADD KEY `idx_courses_level` (`level`);

--
-- Indexes for table `enrollments`
--
ALTER TABLE `enrollments`
  ADD PRIMARY KEY (`enrollment_id`),
  ADD UNIQUE KEY `unique_enrollment` (`user_id`,`course_id`),
  ADD KEY `idx_enrollments_user` (`user_id`),
  ADD KEY `idx_enrollments_course` (`course_id`);

--
-- Indexes for table `lectures`
--
ALTER TABLE `lectures`
  ADD PRIMARY KEY (`lecture_id`),
  ADD KEY `course_id` (`course_id`);

--
-- Indexes for table `password_reset_tokens`
--
ALTER TABLE `password_reset_tokens`
  ADD PRIMARY KEY (`token_id`),
  ADD KEY `user_id` (`user_id`),
  ADD KEY `idx_password_tokens_code` (`verification_code`);

--
-- Indexes for table `reports`
--
ALTER TABLE `reports`
  ADD PRIMARY KEY (`report_id`),
  ADD KEY `idx_reports_type` (`report_type`),
  ADD KEY `idx_reports_date` (`generation_date`);

--
-- Indexes for table `results`
--
ALTER TABLE `results`
  ADD PRIMARY KEY (`result_id`),
  ADD KEY `idx_results_enrollment` (`enrollment_id`);

--
-- Indexes for table `students`
--
ALTER TABLE `students`
  ADD PRIMARY KEY (`student_id`),
  ADD UNIQUE KEY `student_code` (`student_code`),
  ADD KEY `idx_students_level` (`level`);

--
-- Indexes for table `student_results`
--
ALTER TABLE `student_results`
  ADD PRIMARY KEY (`result_id`),
  ADD KEY `course_id` (`course_id`);

--
-- Indexes for table `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`user_id`),
  ADD UNIQUE KEY `username` (`username`),
  ADD UNIQUE KEY `email` (`email`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `courses`
--
ALTER TABLE `courses`
  MODIFY `course_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT for table `enrollments`
--
ALTER TABLE `enrollments`
  MODIFY `enrollment_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT for table `lectures`
--
ALTER TABLE `lectures`
  MODIFY `lecture_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT for table `password_reset_tokens`
--
ALTER TABLE `password_reset_tokens`
  MODIFY `token_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT for table `reports`
--
ALTER TABLE `reports`
  MODIFY `report_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT for table `results`
--
ALTER TABLE `results`
  MODIFY `result_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT for table `student_results`
--
ALTER TABLE `student_results`
  MODIFY `result_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT for table `users`
--
ALTER TABLE `users`
  MODIFY `user_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=7;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `enrollments`
--
ALTER TABLE `enrollments`
  ADD CONSTRAINT `enrollments_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE,
  ADD CONSTRAINT `enrollments_ibfk_2` FOREIGN KEY (`course_id`) REFERENCES `courses` (`course_id`) ON DELETE CASCADE;

--
-- Constraints for table `lectures`
--
ALTER TABLE `lectures`
  ADD CONSTRAINT `lectures_ibfk_1` FOREIGN KEY (`course_id`) REFERENCES `courses` (`course_id`) ON DELETE CASCADE;

--
-- Constraints for table `password_reset_tokens`
--
ALTER TABLE `password_reset_tokens`
  ADD CONSTRAINT `password_reset_tokens_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE;

--
-- Constraints for table `results`
--
ALTER TABLE `results`
  ADD CONSTRAINT `results_ibfk_1` FOREIGN KEY (`enrollment_id`) REFERENCES `enrollments` (`enrollment_id`) ON DELETE CASCADE;

--
-- Constraints for table `students`
--
ALTER TABLE `students`
  ADD CONSTRAINT `students_ibfk_1` FOREIGN KEY (`student_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE;

--
-- Constraints for table `student_results`
--
ALTER TABLE `student_results`
  ADD CONSTRAINT `student_results_ibfk_1` FOREIGN KEY (`course_id`) REFERENCES `courses` (`course_id`) ON DELETE SET NULL;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
