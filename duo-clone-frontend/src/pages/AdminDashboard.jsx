import './AdminDashboard.css'
import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { apiService } from '../services/api'
import { getUser, removeUser } from '../context/UserContext'
import { FaUser, FaBook, FaSignOutAlt, FaChartBar, FaChevronRight } from 'react-icons/fa'
import CompletionRate from '../components/CompletionRate'

function AdminDashboard() {
  const navigate = useNavigate()
  const [user, setUser] = useState(null)
  const [activeTab, setActiveTab] = useState('users')
  const [users, setUsers] = useState([])
  const [courses, setCourses] = useState([])
  const [loading, setLoading] = useState(true)

  // Function to mask email addresses
  const maskEmail = (email) => {
    if (!email) return ''
    const [username, domain] = email.split('@')
    if (!domain) return email
    
    const maskedUsername = username.length > 2 
      ? username[0] + '*'.repeat(username.length - 2) + username[username.length - 1]
      : username[0] + '*'
    
    const [domainName, extension] = domain.split('.')
    const maskedDomain = domainName.length > 2
      ? domainName[0] + '*'.repeat(domainName.length - 2) + domainName[domainName.length - 1]
      : domainName
    
    return `${maskedUsername}@${maskedDomain}.${extension}`
  }
  const [showCourseModal, setShowCourseModal] = useState(false)
  const [stats, setStats] = useState({
    totalUsers: 0,
    totalCourses: 0,
    activeEnrollments: 0,
    completionRate: 0
  })
  const [showProfileMenu, setShowProfileMenu] = useState(false)
  const [showCompletionModal, setShowCompletionModal] = useState(false)

  useEffect(() => {
    const currentUser = getUser()
    
    if (!currentUser) {
      navigate('/signin', { replace: true })
      return
    }

    setUser( currentUser)

    const userRole = currentUser.role?.replace('ROLE_', '')
    if (userRole !== 'ADMIN') {
      navigate('/home', { replace: true })
      return
    }

    fetchAdminData()
  }, [navigate])

  const fetchAdminData = async () => {
    try {
      setLoading(true)
      const [usersData, coursesData] = await Promise.all([
        apiService.getAllUsers(),
        apiService.getAllCourses()
      ])
      
      // Filter out admin users - only show regular users
      const regularUsers = usersData.filter(u => {
        const role = u.role?.replace('ROLE_', '')
        return role !== 'ADMIN'
      })
      
      setUsers(regularUsers)
      setCourses(coursesData)
      
      // Calculate stats (only for regular users)
      const totalEnrollments = regularUsers.reduce((sum, u) => sum + (u.enrolledCourses || 0), 0)
      const completedCourses = regularUsers.reduce((sum, u) => sum + (u.completedCourses || 0), 0)
      
      setStats({
        totalUsers: regularUsers.length,
        totalCourses: coursesData.length,
        activeEnrollments: totalEnrollments,
        completionRate: totalEnrollments > 0 ? Math.round((completedCourses / totalEnrollments) * 100) : 0
      })
    } catch (error) {
      console.error('Error fetching admin data:', error)
    } finally {
      setLoading(false)
    }
  }

  const handleLogout = () => {
    removeUser()
    navigate('/signin')
  }

  const handleAddCourse = () => {
    setShowCourseModal(true)
  }

  const handleUploadJSON = () => {
    setShowCourseModal(false)
    const fileInput = document.createElement('input')
    fileInput.type = 'file'
    fileInput.accept = '.json'
    fileInput.onchange = async (e) => {
      const file = e.target.files[0]
      if (file) {
        try {
          const text = await file.text()
          const courseData = JSON.parse(text)
          
          // Validate JSON structure
          if (!courseData.name || !courseData.description) {
            alert('Invalid JSON: name and description are required')
            return
          }
          
          // Navigate to NewCourse page with pre-filled data
          navigate('/admin/new-course', { state: { courseData } })
        } catch (error) {
          alert('Error reading JSON file. Please check the file format.')
          console.error('JSON parse error:', error)
        }
      }
    }
    fileInput.click()
  }

  const handleManualEntry = () => {
    setShowCourseModal(false)
    navigate('/admin/new-course')
  }

  const handleCloseModal = () => {
    setShowCourseModal(false)
  }

  const handleDeleteUser = async (userId, userName, userRole) => {
    // Extra protection: prevent deleting admin users
    if (userRole === 'ROLE_ADMIN' || userRole === 'ADMIN') {
      alert('Admin users cannot be deleted')
      return
    }

    if (window.confirm(`Are you sure you want to delete user "${userName}"? This action cannot be undone.`)) {
      try {
        await apiService.deleteUser(userId)
        alert('User deleted successfully')
        fetchAdminData() // Refresh the user list
      } catch (error) {
        console.error('Error deleting user:', error)
        alert('Failed to delete user')
      }
    }
  }

  const toggleProfileMenu = () => {
    setShowProfileMenu(!showProfileMenu)
  }

  if (!user) {
    return (
      <div className="admin-dashboard">
        <p style={{ textAlign: 'center', padding: '60px 20px' }}>Loading...</p>
      </div>
    )
  }

  if (loading) {
    return <div className="admin-dashboard"><p>Loading dashboard...</p></div>
  }

  return (
    <div className="admin-dashboard">
      <nav className="admin-navbar">
        <h2>Admin Dashboard</h2>
        <div className="admin-nav-actions">
          <div className="navbar-profile">
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
                      <p className="profile-name">{user?.name || 'Admin'}</p>
                    <p className="profile-email">{user?.email}</p>
                    </div>
                  </div>
                  
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

      <div className="admin-content">
        {/* Stats Overview */}
        <section className="admin-stats">
          <div className="stat-card">
            <FaUser className="stat-icon" />
            <div className="stat-info">
              <span className="stat-number">{stats.totalUsers}</span>
              <span className="stat-label">Total Users</span>
            </div>
          </div>
          <div className="stat-card">
            <FaBook className="stat-icon" />
            <div className="stat-info">
              <span className="stat-number">{stats.totalCourses}</span>
              <span className="stat-label">Total Courses</span>
            </div>
          </div>
          <div className="stat-card">
            <FaChartBar className="stat-icon" />
            <div className="stat-info">
              <span className="stat-number">{stats.activeEnrollments}</span>
              <span className="stat-label">Active Enrollments</span>
            </div>
          </div>
          <div className="stat-card clickable" onClick={() => setShowCompletionModal(true)}>
            <FaChartBar className="stat-icon" />
            <div className="stat-info">
              <span className="stat-number">{stats.completionRate}%</span>
              <span className="stat-label">Completion Rate</span>
            </div>
            <FaChevronRight className="stat-arrow" />
          </div>
        </section>

        {/* Tabs */}
        <div className="admin-tabs">
          <button 
            className={`admin-tab ${activeTab === 'users' ? 'active' : ''}`}
            onClick={() => setActiveTab('users')}
          >
            <FaUser /> Users ({users.length})
          </button>
          <button 
            className={`admin-tab ${activeTab === 'courses' ? 'active' : ''}`}
            onClick={() => setActiveTab('courses')}
          >
            <FaBook /> Courses ({courses.length})
          </button>
        </div>

        {/* Tab Content */}
        <div className="admin-tab-content">
          {activeTab === 'users' && (
            <div className="users-section">
              <div className="section-header">
                <h2>Platform Users</h2>
              </div>
              
              <div className="table-container">
                <table className="admin-table">
                  <thead>
                    <tr>
                      <th>Name</th>
                      <th>Email</th>
                      <th>Role</th>
                      <th>Enrolled Courses</th>
                      <th>Completed</th>
                      <th>Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    {users.map((user, index) => (
                      <tr key={user.id || index}>
                        <td>{user.name}</td>
                        <td>{maskEmail(user.email)}</td>
                        <td>
                          <span className={`role-badge ${user.role?.toLowerCase()}`}>
                            {user.role?.replace('ROLE_', '')}
                          </span>
                        </td>
                        <td>{user.enrolledCourses || 0}</td>
                        <td>{user.completedCourses || 0}</td>
                        <td>
                          <button 
                            className="btn-action" 
                            onClick={() => navigate(`/admin/user/${user.id}`)}
                          >
                            View
                          </button>
                          <button 
                            className="btn-action danger"
                            onClick={() => handleDeleteUser(user.id, user.name, user.role)}
                          >
                            Delete
                          </button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          )}

          {activeTab === 'courses' && (
            <div className="courses-section">
              <div className="section-header">
                <h2>Available Courses</h2>
                <button className="btn-primary" onClick={handleAddCourse}>+ Add Course</button>
              </div>
              
              <div className="courses-grid-admin">
                {courses.map((course, index) => (
                  <div key={course.id || index} className="course-card-admin">
                    <div className="course-header-admin">
                      <h3>{course.name}</h3>
                      <span className="course-badge">Active</span>
                    </div>
                    <p className="course-description-admin">{course.description}</p>
                    <div className="course-meta">
                      <span>📚 {course.totalLessons || 20} lessons</span>
                      <span>⏱️ {course.duration || '~8 hours'}</span>
                    </div>
                    <div className="course-actions">
                      <button 
                        className="btn-action" 
                        onClick={() => navigate(`/admin/edit-course/${course.id}`)}
                      >
                        Edit
                      </button>
                      <button 
                        className="btn-action" 
                        onClick={() => navigate(`/admin/course/${course.id}`)}
                      >
                        Details
                      </button>
                      <button className="btn-action danger">Delete</button>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          )}
        </div>
      </div>

      {/* Course Creation Modal */}
      {showCourseModal && (
        <div className="modal-overlay" onClick={handleCloseModal}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h3>Add New Course</h3>
              <button className="modal-close" onClick={handleCloseModal}>×</button>
            </div>
            <div className="modal-body">
              <p className="modal-description">Choose how you'd like to add a new course:</p>
              
              <div className="modal-options">
                <div className="modal-option-card" onClick={handleUploadJSON}>
                  <div className="option-icon">📄</div>
                  <h4>Upload JSON File</h4>
                  <p>Import course details from a JSON file with all content pre-configured</p>
                </div>
                
                <div className="modal-option-card" onClick={handleManualEntry}>
                  <div className="option-icon">✏️</div>
                  <h4>Manual Entry</h4>
                  <p>Create a course from scratch by entering all details manually</p>
                </div>
              </div>
            </div>
          </div>
        </div>
      )}
      {/* Completion Rate Modal */}
      <CompletionRate 
        isOpen={showCompletionModal}
        onClose={() => setShowCompletionModal(false)}
        stats={stats}
      />    </div>
  )
}

export default AdminDashboard
