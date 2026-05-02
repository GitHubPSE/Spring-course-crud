CREATE TABLE IF NOT EXISTS `User` (
    `User_Id`      VARCHAR(20) NOT NULL,
    `Name`         VARCHAR(10) NOT NULL,
    `Role`         CHAR(10)    NOT NULL COMMENT 'CREATOR / CLASSMATE',
    `Created_Date` DATETIME    NOT NULL DEFAULT NOW(),
    `Updated_Date` DATETIME    NULL,
    `Del_Date`     DATETIME    NULL,
    PRIMARY KEY (`User_Id`)
);

CREATE TABLE IF NOT EXISTS `Course` (
    `Course_No`    BIGINT       NOT NULL AUTO_INCREMENT,
    `Title`        VARCHAR(255) NOT NULL,
    `Description`  TEXT         NOT NULL,
    `Price`        INT          NOT NULL,
    `Max_Capacity` INT          NOT NULL,
    `Start_Date`   DATETIME     NOT NULL,
    `End_Date`     DATETIME     NOT NULL,
    `Status`       VARCHAR(6)   NOT NULL DEFAULT 'DRAFT' COMMENT 'DRAFT / OPEN / CLOSED',
    `Created_Date` DATETIME     NOT NULL DEFAULT NOW(),
    `Updated_Date` DATETIME     NULL,
    `Del_Date`     DATETIME     NULL,
    `User_Id`      VARCHAR(20)  NOT NULL COMMENT '강좌 생성 ID',
    PRIMARY KEY (`Course_No`),
    FOREIGN KEY (`User_Id`) REFERENCES `User`(`User_Id`)
);

CREATE TABLE IF NOT EXISTS `Enrollment` (
    `Enrollment_No`  BIGINT      NOT NULL AUTO_INCREMENT,
    `Status`         VARCHAR(10) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING / CONFIRMED / CANCELLED / WAITLISTED',
    `Confirmed_Date` DATETIME    NULL,
    `Cancelled_Date` DATETIME    NULL,
    `Created_Date`   DATETIME    NOT NULL,
    `Updated_Date`   DATETIME    NULL,
    `Waitlist_Order` INT         NULL,
    `Course_No`      BIGINT      NOT NULL,
    `User_Id`        VARCHAR(20) NOT NULL,
    PRIMARY KEY (`Enrollment_No`),
    FOREIGN KEY (`Course_No`) REFERENCES `Course`(`Course_No`),
    FOREIGN KEY (`User_Id`)   REFERENCES `User`(`User_Id`)
);

-- 초기 사용자 데이터 (CREATOR 3명, CLASSMATE 10명)
INSERT IGNORE INTO `User` (`User_Id`, `Name`, `Role`, `Created_Date`) VALUES
('kim_creat1',  '김강사',  'CREATOR',   NOW()),
('lee_creat1',  '이강사',  'CREATOR',   NOW()),
('park_creat1', '박강사',  'CREATOR',   NOW()),
('jung_class1', '정수강',  'CLASSMATE', NOW()),
('choi_class1', '최수강',  'CLASSMATE', NOW()),
('kang_class1', '강수강',  'CLASSMATE', NOW()),
('yoon_class1', '윤수강',  'CLASSMATE', NOW()),
('jang_class1', '장수강',  'CLASSMATE', NOW()),
('lim_class1',  '임수강',  'CLASSMATE', NOW()),
('han_class1',  '한수강',  'CLASSMATE', NOW()),
('oh_class1',   '오수강',  'CLASSMATE', NOW()),
('seo_class1',  '서수강',  'CLASSMATE', NOW()),
('shin_class1', '신수강',  'CLASSMATE', NOW());
