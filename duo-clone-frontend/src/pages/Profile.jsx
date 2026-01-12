import './Profile.css'
import { useNavigate } from 'react-router-dom'
import { useState, useEffect, useContext } from 'react'
import { apiService } from '../services/api'
import { getUser, removeUser } from '../context/UserContext'
import { FaUser, FaChartBar, FaCog, FaSignOutAlt, FaChevronLeft, FaMoon, FaSun } from 'react-icons/fa'
import { ThemeContext } from '../theme'

function Profile() {
  const navigate = useNavigate()
  const [activeTab, setActiveTab] = useState('overview')
  const [loading, setLoading] = useState(true)
  const [currentUser, setCurrentUser] = useState(null)
  const [showProfileMenu, setShowProfileMenu] = useState(false)
  const [showEditModal, setShowEditModal] = useState(false)
  const [editName, setEditName] = useState('')
  const [editLoading, setEditLoading] = useState(false)
  const [editError, setEditError] = useState('')
  const { theme, toggleTheme } = useContext(ThemeContext)

  // Format join date from user's createdAt
  const formatJoinDate = (createdAt) => {
    if (!createdAt) return 'January 2024'
    
    try {
      const date = new Date(createdAt)
      const options = { month: 'long', year: 'numeric' }
      return date.toLocaleDateString('en-US', options)
    } catch (error) {
      console.error('Error formatting date:', error)
      return 'January 2024'
    }
  }

  // Default user data (fallback)
  const [user, setUser] = useState({
    name: currentUser?.name || 'Demo User',
    email: currentUser?.email || 'demo@techlingo.com',
    joinDate: formatJoinDate(currentUser?.createdAt),
    avatar: '👤',
    stats: {
      coursesCompleted: 0,
      coursesInProgress: 0,
      totalXP: 0,
      currentStreak: 0,
      longestStreak: 0,
      lessonsCompleted: 0,
      exercisesDone: 0,
      badges: 0
    },
    enrolledCourses: [], // Add enrolled courses data
    achievements: [
      { id: 1, icon: '🔥', name: 'Week Warrior', description: '7-day streak' },
      { id: 2, icon: '⚡', name: 'Quick Learner', description: 'Complete 10 lessons' },
      { id: 3, icon: '🎯', name: 'Sharpshooter', description: '100% on a quiz' },
      { id: 4, icon: '📚', name: 'Bookworm', description: 'Start 5 courses' },
      { id: 5, icon: '💪', name: 'Dedicated', description: '30 exercises done' },
      { id: 6, icon: '🏆', name: 'Champion', description: 'Complete a course' },
      { id: 7, icon: '⭐', name: 'Rising Star', description: 'Earn 1000 XP' },
      { id: 8, icon: '🚀', name: 'Rocket Start', description: 'First lesson done' }
    ],
    recentActivity: [
      { id: 1, action: 'Completed lesson', course: 'Java Programming', time: '2 hours ago' },
      { id: 2, action: 'Earned badge', course: 'Quick Learner', time: '1 day ago' },
      { id: 3, action: 'Started course', course: 'Python Basics', time: '3 days ago' },
      { id: 4, action: 'Completed quiz', course: 'Java Programming', time: '4 days ago' }
    ]
  })

  // Redirect to signin if not authenticated
  useEffect(() => {
    const user = getUser()
    if (!user) {
      navigate('/signin', { replace: true })
      return
    }
    setCurrentUser(user)
  }, [navigate])

  useEffect(() => {
    const fetchUserData = async () => {
      if (!currentUser) return
      
      try {
        setLoading(true)
        const enrolledCourses = await apiService.getEnrolledCourses()
        
        // Fetch detailed progress for each course
        const coursesWithDetails = await Promise.all(
          enrolledCourses.map(async (course) => {
            try {
              const details = await apiService.getCourseDetails(course.id)
              const progressData = await apiService.getCourseProgress(course.id)
              
              return {
                ...course,
                ...details,
                technicalProgress: progressData?.technicalProgress || [],
                mcqProgress: progressData?.mcqProgress || [],
                technicalCompleted: progressData?.technicalProgress?.filter(Boolean).length || 0,
                mcqCompleted: progressData?.mcqProgress?.filter(Boolean).length || 0,
                totalTechnical: details.technicalContent?.length || 0,
                totalMcq: details.mcqQuestions?.length || 0
              }
            } catch (error) {
              console.error(`Error fetching details for course ${course.id}:`, error)
              return course
            }
          })
        )
        
        setUser(prev => ({
          ...prev,
          name: currentUser.name || prev.name,
          email: currentUser.email || prev.email,
          joinDate: formatJoinDate(currentUser.createdAt),
          enrolledCourses: coursesWithDetails
        }))
        
        // Update user stats
        if (Array.isArray(coursesWithDetails) && coursesWithDetails.length > 0) {
          const completedCount = coursesWithDetails.filter(c => c.progress >= 100).length
          const inProgressCount = coursesWithDetails.filter(c => c.progress > 0 && c.progress < 100).length
          
          // Calculate total lessons completed
          const totalLessonsCompleted = coursesWithDetails.reduce((sum, c) => {
            return sum + (c.technicalCompleted || 0) + (c.mcqCompleted || 0)
          }, 0)
          
          setUser(prev => ({
            ...prev,
            stats: {
              ...prev.stats,
              coursesCompleted: completedCount,
              coursesInProgress: inProgressCount,
              lessonsCompleted: totalLessonsCompleted
            }
          }))
        }
      } catch (error) {
        console.error('Error fetching user data:', error)
      } finally {
        setLoading(false)
      }
    }

    if (currentUser) {
      fetchUserData()
    }
  }, [currentUser])

  const handleLogout = () => {
    removeUser()
    navigate('/signin')
  }

  const toggleProfileMenu = () => {
    setShowProfileMenu(!showProfileMenu)
  }

  const handleEditProfile = () => {
    setEditName(user.name)
    setEditError('')
    setShowEditModal(true)
  }

  const handleSaveProfile = async () => {
    if (!editName.trim()) {
      setEditError('Name cannot be empty')
      return
    }

    setEditLoading(true)
    setEditError('')

    try {
      const response = await apiService.updateUserProfile({ name: editName.trim() })
      
      // Update local state
      setUser(prev => ({
        ...prev,
        name: response.name
      }))
      
      // Update current user context
      const updatedUser = { ...currentUser, name: response.name }
      setCurrentUser(updatedUser)
      localStorage.setItem('user', JSON.stringify(updatedUser))
      
      setShowEditModal(false)
      alert('Profile updated successfully!')
    } catch (error) {
      console.error('Error updating profile:', error)
      setEditError(error.response?.data?.error || 'Failed to update profile')
    } finally {
      setEditLoading(false)
    }
  }

  if (!currentUser) {
    return <div className="profile-page"><p>Loading...</p></div>
  }

  if (loading) {
    return <div className="profile-page"><p>Loading profile...</p></div>
  }

  return (
    <div className="profile-page">
      <nav className="navbar">
        <div className="navbar-left">
          <button className="btn-back-arrow" onClick={() => navigate('/home')} aria-label="Back to Home">
            <FaChevronLeft />
          </button>
          <h2 onClick={() => navigate('/home')} style={{ cursor: 'pointer' }}>TechLingo</h2>
        </div>
        
        <div className="navbar-right">
          <button className="btn-theme-toggle" onClick={toggleTheme} aria-label="Toggle theme">
            {theme === 'light' ? <FaMoon /> : <FaSun />}
          </button>
          
          <div className="navbar-profile">
            <span className="navbar-username">{user.name}</span>
            <button className="profile-btn" onClick={toggleProfileMenu}>
              <FaUser className="profile-icon" />
            </button>
            
            {showProfileMenu && (
              <>
                <div className="profile-overlay" onClick={() => setShowProfileMenu(false)} />
                <div className="profile-menu">
                  <div className="profile-menu-header">
                    <div className="profile-avatar">
                      <FaUser size={24} />
                    </div>
                    <div className="profile-info">
                      <p className="profile-name">{user.name}</p>
                      <p className="profile-email">{user.email}</p>
                    </div>
                  </div>
                  
                  <div className="profile-menu-divider" />
                  
                  <button className="profile-menu-item" onClick={() => { setShowProfileMenu(false); navigate('/home'); }}>
                    <FaChartBar className="menu-icon" />
                    Back to Home
                  </button>
                  
                  <div className="profile-menu-divider" />
                  
                  <button className="profile-menu-item logout" onClick={handleLogout}>
                    <FaSignOutAlt className="menu-icon" />
                    Logout
                  </button>
                </div>
              </>
            )}
          </div>
        </div>
      </nav>

      <div className="profile-content">
        {/* Profile Header */}
        <section className="profile-header-section">
          <div className="profile-header-card">
            <div className="profile-avatar-large">{user.avatar}</div>
            <div className="profile-header-info">
              <h1>{user.name}</h1>
              <p className="profile-email-display">{user.email}</p>
              <p className="profile-join-date">Member since {user.joinDate}</p>
            </div>
            <div className="profile-header-actions">
              <button className="btn-edit-profile" onClick={handleEditProfile}>Edit Profile</button>
            </div>
          </div>
        </section>

        {/* Tabs */}
        <div className="profile-tabs">
          <button 
            className={`tab-btn ${activeTab === 'overview' ? 'active' : ''}`}
            onClick={() => setActiveTab('overview')}
          >
            Overview
          </button>
          <button 
            className={`tab-btn ${activeTab === 'achievements' ? 'active' : ''}`}
            onClick={() => setActiveTab('achievements')}
          >
            Achievements
          </button>
          <button 
            className={`tab-btn ${activeTab === 'activity' ? 'active' : ''}`}
            onClick={() => setActiveTab('activity')}
          >
            Activity
          </button>
        </div>

        {/* Tab Content */}
        <div className="tab-content">
          {activeTab === 'overview' && (
            <div className="overview-content">
              {/* Course Progress Section */}
              {user.enrolledCourses && user.enrolledCourses.length > 0 && (
                <section className="course-progress-overview">
                  <h3>Your Courses</h3>
                  <div className="course-progress-list">
                    {user.enrolledCourses.map((course) => (
                      <div key={course.id} className="course-progress-card">
                        <div className="course-progress-header">
                          <div className="course-info-compact">
                            <h4>{course.name}</h4>
                            <span className="course-level-badge">{course.level}</span>
                          </div>
                          <span className="progress-percent-large">{course.progress || 0}%</span>
                        </div>
                        
                        <div className="progress-bar-profile">
                          <div 
                            className="progress-fill-profile" 
                            style={{ width: `${course.progress || 0}%` }}
                          />
                        </div>
                        
                        <div className="course-progress-stats">
                          <div className="progress-stat">
                            <span className="stat-icon">📚</span>
                            <span className="stat-text">
                              {course.technicalCompleted || 0}/{course.totalTechnical || 0} Technical
                            </span>
                          </div>
                          <div className="progress-stat">
                            <span className="stat-icon">❓</span>
                            <span className="stat-text">
                              {course.mcqCompleted || 0}/{course.totalMcq || 0} Quiz
                            </span>
                          </div>
                        </div>
                        
                        <button 
                          className="btn-continue-profile"
                          onClick={() => navigate(`/course-continue/${course.id}`)}
                        >
                          Continue Learning →
                        </button>
                      </div>
                    ))}
                  </div>
                </section>
              )}

              <div className="overview-grid">
                <div className="overview-card">
                  <h3>Learning Progress</h3>
                  <div className="progress-item">
                    <span className="progress-label">Courses Completed</span>
                    <span className="progress-value">{user.stats.coursesCompleted}</span>
                  </div>
                  <div className="progress-item">
                    <span className="progress-label">Courses In Progress</span>
                    <span className="progress-value">{user.stats.coursesInProgress}</span>
                  </div>
                  <div className="progress-item">
                    <span className="progress-label">Modules Completed</span>
                    <span className="progress-value">{user.stats.lessonsCompleted}</span>
                  </div>
                  <div className="progress-item">
                    <span className="progress-label">Longest Streak</span>
                    <span className="progress-value">{user.stats.longestStreak} days</span>
                  </div>
                </div>

                <div className="overview-card">
                  <h3>Recent Badges</h3>
                  <div className="badges-preview">
                    {user.achievements.slice(0, 4).map(badge => (
                      <div key={badge.id} className="badge-item-small">
                        <span className="badge-icon-small">{badge.icon}</span>
                        <span className="badge-name-small">{badge.name}</span>
                      </div>
                    ))}
                  </div>
                  <button className="btn-view-all" onClick={() => setActiveTab('achievements')}>
                    View All Badges →
                  </button>
                </div>
              </div>
            </div>
          )}

          {activeTab === 'achievements' && (
            <div className="achievements-content">
              <h3>All Achievements</h3>
              <div className="achievements-grid">
                {user.achievements.map(badge => (
                  <div key={badge.id} className="achievement-card">
                    <span className="achievement-icon">{badge.icon}</span>
                    <h4>{badge.name}</h4>
                    <p>{badge.description}</p>
                  </div>
                ))}
              </div>
            </div>
          )}

          {activeTab === 'activity' && (
            <div className="activity-content">
              <h3>Recent Activity</h3>
              <div className="activity-list">
                {user.recentActivity.map(item => (
                  <div key={item.id} className="activity-item">
                    <div className="activity-dot"></div>
                    <div className="activity-info">
                      <p className="activity-action">{item.action}: <strong>{item.course}</strong></p>
                      <span className="activity-time">{item.time}</span>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          )}
        </div>
      </div>

      {/* Edit Profile Modal */}
      {showEditModal && (
        <>
          <div className="modal-overlay" onClick={() => !editLoading && setShowEditModal(false)} />
          <div className="edit-profile-modal">
            <div className="modal-header">
              <h3>Edit Profile</h3>
              <button className="modal-close" onClick={() => setShowEditModal(false)} disabled={editLoading}>×</button>
            </div>
            <div className="modal-body">
              {editError && <div className="error-message">{editError}</div>}
              
              <div className="form-group">
                <label htmlFor="edit-name">Name</label>
                <input
                  type="text"
                  id="edit-name"
                  value={editName}
                  onChange={(e) => setEditName(e.target.value)}
                  placeholder="Enter your name"
                  disabled={editLoading}
                  autoFocus
                />
              </div>

              <div className="form-group">
                <label htmlFor="edit-email">Email (cannot be changed)</label>
                <input
                  type="email"
                  id="edit-email"
                  value={user.email}
                  disabled
                  style={{ opacity: 0.6, cursor: 'not-allowed' }}
                />
              </div>
            </div>
            <div className="modal-actions">
              <button 
                className="btn-cancel" 
                onClick={() => setShowEditModal(false)}
                disabled={editLoading}
              >
                Cancel
              </button>
              <button 
                className="btn-save" 
                onClick={handleSaveProfile}
                disabled={editLoading || !editName.trim()}
              >
                {editLoading ? 'Saving...' : 'Save Changes'}
              </button>
            </div>
          </div>
        </>
      )}
    </div>
  )
}

export default Profile
