import './UserView.css'
import { useState, useEffect } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { apiService } from '../services/api'
import { 
  FaChevronLeft, 
  FaTrash, 
  FaUser, 
  FaEnvelope, 
  FaIdBadge, 
  FaBook, 
  FaTrophy, 
  FaGamepad,
  FaHeart,
  FaClock,
  FaCheckCircle,
  FaChartLine,
  FaListAlt,
  FaCalendar
} from 'react-icons/fa'
import { getUser } from '../context/UserContext'

function UserView() {
  const navigate = useNavigate()
  const { id } = useParams()
  const [currentUser, setCurrentUser] = useState(null)
  const [userData, setUserData] = useState(null)
  const [loading, setLoading] = useState(true)
  const [showDeleteModal, setShowDeleteModal] = useState(false)

  useEffect(() => {
    const user = getUser()
    if (!user) {
      navigate('/signin', { replace: true })
      return
    }
    setCurrentUser(user)

    const userRole = user.role?.replace('ROLE_', '')
    if (userRole !== 'ADMIN') {
      navigate('/home', { replace: true })
    }
  }, [navigate])

  useEffect(() => {
    const fetchUserData = async () => {
      if (!currentUser) return
      
      try {
        setLoading(true)
        const data = await apiService.getUserById(id)
        setUserData(data)
      } catch (error) {
        console.error('Error fetching user:', error)
      } finally {
        setLoading(false)
      }
    }

    if (currentUser) {
      fetchUserData()
    }
  }, [id, currentUser])

  const handleDelete = async () => {
    try {
      await apiService.deleteUser(id)
      alert('User deleted successfully')
      navigate('/admin')
    } catch (error) {
      console.error('Error deleting user:', error)
      alert('Failed to delete user')
    }
  }

  if (!currentUser || loading) {
    return (
      <div className="user-view">
        <div className="loading-container">
          <p>Loading...</p>
        </div>
      </div>
    )
  }

  if (!userData) {
    return (
      <div className="user-view">
        <div className="error-container">
          <h2>User not found</h2>
          <button className="btn-primary" onClick={() => navigate('/admin')}>
            Back to Admin
          </button>
        </div>
      </div>
    )
  }

  const isAdminUser = userData.role === 'ADMIN' || userData.role === 'ROLE_ADMIN'

  return (
    <div className="user-view">
      <div className="user-view-header">
        <div className="header-left">
          <button className="btn-back-arrow" onClick={() => navigate('/admin')} aria-label="Back to Admin">
            <FaChevronLeft />
          </button>
          <div className="header-icon">
            <FaUser size={48} />
          </div>
          <div className="header-info">
            <h1>{userData.name}</h1>
            <p className="user-email">{userData.email}</p>
            <span className={`role-badge ${userData.role?.toLowerCase()}`}>
              {userData.role?.replace('ROLE_', '')}
            </span>
            {userData.memberSince && (
              <p className="member-since">
                <FaCalendar /> Member since {userData.memberSince}
              </p>
            )}
          </div>
        </div>
        <div className="header-actions">
          {!isAdminUser && (
            <button className="btn-delete" onClick={() => setShowDeleteModal(true)}>
              <FaTrash /> Delete User
            </button>
          )}
        </div>
      </div>

      <div className="user-view-content">
        {/* Quick Stats Grid - Show First */}
        <div className="quick-stats-grid">
          <div className="stat-card">
            <div className="stat-icon enrolled">
              <FaBook />
            </div>
            <div className="stat-info">
              <h3>{userData.enrolledCourses || 0}</h3>
              <p>Enrolled Courses</p>
            </div>
          </div>

          <div className="stat-card">
            <div className="stat-icon completed">
              <FaTrophy />
            </div>
            <div className="stat-info">
              <h3>{userData.completedCourses || 0}</h3>
              <p>Completed Courses</p>
            </div>
          </div>

          <div className="stat-card">
            <div className="stat-icon active">
              <FaChartLine />
            </div>
            <div className="stat-info">
              <h3>{userData.activeCourses || 0}</h3>
              <p>Active Courses</p>
            </div>
          </div>

          <div className="stat-card">
            <div className="stat-icon overall">
              <FaCheckCircle />
            </div>
            <div className="stat-info">
              <h3>{userData.statistics?.overallCompletionPercentage || 0}%</h3>
              <p>Overall Progress</p>
            </div>
          </div>
        </div>

        {/* User Details Grid */}
        <div className="section-card">
          <h2 className="section-title">
            <FaIdBadge /> User Details
          </h2>
          <div className="user-details-grid">
            <div className="detail-card">
              <div className="detail-icon">
                <FaIdBadge />
              </div>
              <div className="detail-info">
                <span className="detail-label">User ID</span>
                <span className="detail-value">{userData.id}</span>
              </div>
            </div>

            <div className="detail-card">
              <div className="detail-icon">
                <FaUser />
              </div>
              <div className="detail-info">
                <span className="detail-label">Full Name</span>
                <span className="detail-value">{userData.name}</span>
              </div>
            </div>

            <div className="detail-card">
              <div className="detail-icon">
                <FaEnvelope />
              </div>
              <div className="detail-info">
                <span className="detail-label">Email Address</span>
                <span className="detail-value">{userData.email}</span>
              </div>
            </div>

            <div className="detail-card">
              <div className="detail-icon">
                <FaUser />
              </div>
              <div className="detail-info">
                <span className="detail-label">Role</span>
                <span className="detail-value">{userData.role?.replace('ROLE_', '')}</span>
              </div>
            </div>
          </div>
        </div>

        {/* Learning Statistics */}
        {userData.statistics && (
          <div className="section-card">
            <h2 className="section-title">
              <FaChartLine /> Learning Statistics
            </h2>
            <div className="stats-grid">
              <div className="stat-item">
                <span className="stat-label">Technical Modules</span>
                <span className="stat-value">
                  {userData.statistics.completedTechnicalModules} / {userData.statistics.totalTechnicalModules}
                </span>
                <div className="progress-bar">
                  <div 
                    className="progress-fill"
                    style={{ 
                      width: `${userData.statistics.totalTechnicalModules > 0 
                        ? (userData.statistics.completedTechnicalModules / userData.statistics.totalTechnicalModules * 100) 
                        : 0}%` 
                    }}
                  ></div>
                </div>
              </div>
              
              <div className="stat-item">
                <span className="stat-label">MCQ Questions</span>
                <span className="stat-value">
                  {userData.statistics.completedMcqQuestions} / {userData.statistics.totalMcqQuestions}
                </span>
                <div className="progress-bar">
                  <div 
                    className="progress-fill mcq"
                    style={{ 
                      width: `${userData.statistics.totalMcqQuestions > 0 
                        ? (userData.statistics.completedMcqQuestions / userData.statistics.totalMcqQuestions * 100) 
                        : 0}%` 
                    }}
                  ></div>
                </div>
              </div>
            </div>
          </div>
        )}

        {/* Enrolled Courses Details */}
        {userData.courseDetails && userData.courseDetails.length > 0 && (
          <div className="section-card">
            <h2 className="section-title">
              <FaBook /> Enrolled Courses
            </h2>
            <div className="courses-list">
              {userData.courseDetails.map((course, index) => (
                <div key={index} className={`course-item ${course.completed ? 'completed' : ''}`}>
                  <div className="course-header-row">
                    <div className="course-info">
                      <h3>{course.courseName}</h3>
                      <span className={`level-badge ${course.courseLevel?.toLowerCase()}`}>
                        {course.courseLevel}
                      </span>
                      {course.completed && <FaCheckCircle className="completed-icon" />}
                    </div>
                    <div className="course-progress-circle">
                      <span className="progress-text">{course.progress || 0}%</span>
                    </div>
                  </div>
                  
                  <div className="course-details-grid">
                    <div className="detail-item">
                      <FaCalendar className="detail-icon" />
                      <span className="detail-text">
                        Enrolled: {course.enrolledAt || 'N/A'}
                      </span>
                    </div>
                    
                    <div className="detail-item">
                      <FaClock className="detail-icon" />
                      <span className="detail-text">
                        Last Active: {course.lastAccessed || 'N/A'}
                      </span>
                    </div>
                  </div>

                  <div className="module-progress">
                    <div className="module-stat">
                      <FaListAlt />
                      <span>Technical: {course.technicalModulesCompleted}/{course.technicalModulesTotal}</span>
                      <div className="mini-progress">
                        <div 
                          className="mini-progress-fill"
                          style={{ 
                            width: `${course.technicalModulesTotal > 0 
                              ? (course.technicalModulesCompleted / course.technicalModulesTotal * 100) 
                              : 0}%` 
                          }}
                        ></div>
                      </div>
                    </div>
                    
                    <div className="module-stat">
                      <FaCheckCircle />
                      <span>MCQ: {course.mcqQuestionsCompleted}/{course.mcqQuestionsTotal}</span>
                      <div className="mini-progress">
                        <div 
                          className="mini-progress-fill mcq"
                          style={{ 
                            width: `${course.mcqQuestionsTotal > 0 
                              ? (course.mcqQuestionsCompleted / course.mcqQuestionsTotal * 100) 
                              : 0}%` 
                          }}
                        ></div>
                      </div>
                    </div>
                  </div>

                  {course.currentSection && (
                    <div className="current-position">
                      <span className="position-label">Current Position:</span>
                      <span className="position-value">
                        {course.currentSection} - Module {(course.currentIndex || 0) + 1}
                      </span>
                    </div>
                  )}
                </div>
              ))}
            </div>
          </div>
        )}

        {/* Game Progress */}
        {userData.gameProgress && userData.gameProgress.coursesWithProgress > 0 && (
          <div className="section-card">
            <h2 className="section-title">
              <FaGamepad /> Game Progress
            </h2>
            <div className="game-stats-overview">
              <div className="game-stat">
                <FaTrophy className="game-icon" />
                <div>
                  <h4>{userData.gameProgress.totalPoints}</h4>
                  <p>Total Points</p>
                </div>
              </div>
              <div className="game-stat">
                <FaChartLine className="game-icon" />
                <div>
                  <h4>{userData.gameProgress.averageLevel}</h4>
                  <p>Average Level</p>
                </div>
              </div>
              <div className="game-stat">
                <FaBook className="game-icon" />
                <div>
                  <h4>{userData.gameProgress.coursesWithProgress}</h4>
                  <p>Courses Played</p>
                </div>
              </div>
            </div>

            {userData.gameProgress.details && userData.gameProgress.details.length > 0 && (
              <div className="game-details-list">
                {userData.gameProgress.details.map((progress, index) => (
                  <div key={index} className="game-detail-item">
                    <div className="game-course-name">{progress.courseName}</div>
                    <div className="game-stats-row">
                      <span className="game-badge">Level {progress.level}</span>
                      <span className="game-badge points">{progress.points} pts</span>
                      <span className="game-badge terms">{progress.knownTermsCount} terms</span>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        )}

        {/* Liked Courses */}
        {userData.likedCourses && userData.likedCourses.length > 0 && (
          <div className="section-card">
            <h2 className="section-title">
              <FaHeart /> Liked Courses ({userData.likedCourses.length})
            </h2>
            <div className="liked-courses-grid">
              {userData.likedCourses.map((course, index) => (
                <div key={index} className="liked-course-card">
                  <div className="liked-course-header">
                    <FaHeart className="heart-icon" />
                    <h4>{course.courseName}</h4>
                  </div>
                  <span className={`level-badge ${course.courseLevel?.toLowerCase()}`}>
                    {course.courseLevel}
                  </span>
                  <p className="liked-course-desc">{course.courseDescription}</p>
                </div>
              ))}
            </div>
          </div>
        )}

        {/* User Details Grid - Removed from here, now at the top */}
      </div>

      {/* Delete Confirmation Modal */}
      {showDeleteModal && (
        <div className="modal-overlay" onClick={() => setShowDeleteModal(false)}>
          <div className="modal-content delete-modal" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h3>⚠️ Delete User</h3>
              <button className="modal-close" onClick={() => setShowDeleteModal(false)}>×</button>
            </div>
            <div className="modal-body">
              <div className="warning-box">
                <p className="warning-text">
                  <strong>Warning:</strong> You are about to delete user <strong>"{userData.name}"</strong>.
                </p>
                <p className="warning-text">
                  This action will permanently remove:
                </p>
                <ul className="warning-list">
                  <li>User account and profile</li>
                  <li>All course enrollments</li>
                  <li>Learning progress and statistics</li>
                  <li>Account preferences and settings</li>
                </ul>
                <p className="warning-text danger">
                  <strong>This action cannot be undone!</strong>
                </p>
              </div>
              
              <div className="modal-actions">
                <button className="btn-secondary" onClick={() => setShowDeleteModal(false)}>
                  Cancel
                </button>
                <button className="btn-danger-confirm" onClick={handleDelete}>
                  Yes, Delete User
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}

export default UserView
