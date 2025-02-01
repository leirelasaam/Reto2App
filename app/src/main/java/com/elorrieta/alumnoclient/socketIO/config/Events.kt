package com.elorrieta.alumnoclient.socketIO.config

/**
 * The events our client is willing to listen or able to send. It is
 * the same class as in the Java Server
 */
enum class Events(val value: String) {
    ON_LOGIN ("onLogin"),
    ON_LOGOUT ("onLogout"),
    ON_LOGIN_ANSWER ("onLoginAnswer"),
    ON_RESET_PASS_EMAIL ("onResetPassEmail"),
    ON_RESET_PASS_EMAIL_ANSWER ("onResetPassEmailAnswer"),
    ON_TEACHER_SCHEDULE("onTeacherSchedule"),
    ON_TEACHER_SCHEDULE_ANSWER("onTeacherScheduleAnswer"),
    ON_STUDENT_DOCUMENTS("onStudentDocuments"),
    ON_STUDENT_DOCUMENTS_ANSWER("onStudentDocumentsAnswer"),
    ON_STUDENT_SCHEDULE("onStudentSchedule"),
    ON_STUDENT_SCHEDULE_ANSWER("onStudentScheduleAnswer"),
    ON_GET_ALL_USERS_ANSWER ("onGetAllUsersAnswer"),
    ON_GET_ALL_USERS("onGetAllUsers"),
    ON_CREATE_MEETING("onCreateMeeting"),
    ON_CREATE_MEETING_ANSWER("onCreateMeetingAnswer"),
    ON_UPDATE_PASS_ANSWER("onUpdatePassAnswer"),
    ON_UPDATE_PASS("onUpdatePass"),
    ON_ALL_MEETINGS("onAllMeetings"),
    ON_ALL_MEETINGS_ANSWER("onAllMeetingsAnswer"),
    ON_PARTICIPANT_STATUS_UPDATE("onParticipantStatusUpdate"),
    ON_MEETING_STATUS_UPDATE("onMeetingStatusUpdate"),
    ON_MEETING_STATUS_UPDATE_ANSWER("onMeetingStatusUpdateAnswer");
}