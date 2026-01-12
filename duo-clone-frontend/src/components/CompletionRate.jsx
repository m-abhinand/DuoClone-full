import './CompletionRate.css'
import { FaTimes, FaGraduationCap, FaChartLine } from 'react-icons/fa'

function CompletionRate({ isOpen, onClose, stats }) {
  if (!isOpen) return null

  const { completionRate, activeEnrollments, totalUsers } = stats || {}
  const completedCourses = Math.round((activeEnrollments * completionRate) / 100) || 0

  // Determine status level based on completion rate
  const getStatusInfo = (rate) => {
    if (rate >= 60) return { label: 'Excellent', color: '#22c55e', class: 'excellent' }
    if (rate >= 40) return { label: 'Good', color: '#3b82f6', class: 'good' }
    if (rate >= 20) return { label: 'Average', color: '#f59e0b', class: 'average' }
    return { label: 'Needs Attention', color: '#ef4444', class: 'poor' }
  }

  const statusInfo = getStatusInfo(completionRate || 0)

  return (
    <div className="completion-modal-overlay" onClick={onClose}>
      <div className="completion-modal" onClick={(e) => e.stopPropagation()}>
        <button className="completion-close-btn" onClick={onClose}>
          ×
        </button>

        <div className="completion-header">
          <div className="completion-icon-badge">
            <FaGraduationCap size={20} />
          </div>
          <h2>Completion Rate</h2>
        </div>

        <div className="completion-body">
          {/* Main Rate Display with Statistics */}
          <div className="completion-rate-display" style={{ width: '100%' }}>
            <div
              className="rate-main"
              style={{
                display: 'grid',
                gridTemplateColumns: 'minmax(240px, 320px) 1fr',
                gap: '24px',
                alignItems: 'stretch',
                width: '100%'
              }}
            >
              <div className="rate-section">
                <div className="rate-circle">
                  <svg viewBox="0 0 100 100" className="rate-progress-ring">
                    <circle cx="50" cy="50" r="45" className="rate-progress-bg" />
                    <circle 
                      cx="50" 
                      cy="50" 
                      r="45" 
                      className={`rate-progress-fill ${statusInfo.class}`}
                      style={{
                        strokeDasharray: `${2 * Math.PI * 45}`,
                        strokeDashoffset: `${2 * Math.PI * 45 * (1 - (completionRate || 0) / 100)}`,
                        stroke: statusInfo.color
                      }}
                    />
                  </svg>
                  <div className="rate-value">
                    <span className="rate-number" style={{ color: statusInfo.color }}>{completionRate || 0}</span>
                    <span className="rate-percent">%</span>
                  </div>
                </div>
                <div className="rate-info">
                  <span className="status-badge" style={{ background: statusInfo.color }}>{statusInfo.label}</span>
                  <p className="status-description">Current rate</p>
                </div>
              </div>

              {/* Statistics Grid */}
              <div
                className="stats-grid"
                style={{
                  display: 'grid',
                  gridTemplateColumns: 'repeat(2, 1fr)',
                  gap: '16px',
                  width: '100%',
                  alignSelf: 'stretch'
                }}
              >
                <div className="stat-item">
                  <span className="stat-value">{activeEnrollments || 0}</span>
                  <span className="stat-label">Enrollments</span>
                </div>
                <div className="stat-item">
                  <span className="stat-value">{completedCourses}</span>
                  <span className="stat-label">Completed</span>
                </div>
                <div className="stat-item">
                  <span className="stat-value">{totalUsers || 0}</span>
                  <span className="stat-label">Active Users</span>
                </div>
                <div className="stat-item">
                  <span className="stat-value">
                    {totalUsers > 0 ? ((activeEnrollments || 0) / totalUsers).toFixed(1) : 0}
                  </span>
                  <span className="stat-label">Avg/User</span>
                </div>
              </div>
            </div>
          </div>

          {/* What is Completion Rate */}
          <div className="completion-section info-section">
            <div className="section-header">
              <FaChartLine className="section-icon" />
              <h3>About Completion Rate</h3>
            </div>
            <p className="section-text">
              Measures the percentage of enrolled courses successfully completed. 
              This metric reflects user engagement and content effectiveness.
            </p>
            <div className="formula-compact">
              <span className="formula-text">Formula:</span>
              <span className="formula-expression">(Completed ÷ Enrolled) × 100</span>
            </div>
          </div>

          {/* Benchmarks */}
          <div className="completion-section benchmarks">
            <h3>Industry Benchmarks</h3>
            <div className="benchmark-bars">
              <div className="benchmark-item">
                <div className="benchmark-label">
                  <span className="benchmark-name">Excellent</span>
                  <span className="benchmark-range">60%+</span>
                </div>
                <div className="benchmark-bar">
                  <div className="benchmark-fill excellent" style={{ width: '100%' }}></div>
                </div>
              </div>
              <div className="benchmark-item">
                <div className="benchmark-label">
                  <span className="benchmark-name">Good</span>
                  <span className="benchmark-range">40-60%</span>
                </div>
                <div className="benchmark-bar">
                  <div className="benchmark-fill good" style={{ width: '75%' }}></div>
                </div>
              </div>
              <div className="benchmark-item">
                <div className="benchmark-label">
                  <span className="benchmark-name">Average</span>
                  <span className="benchmark-range">20-40%</span>
                </div>
                <div className="benchmark-bar">
                  <div className="benchmark-fill average" style={{ width: '50%' }}></div>
                </div>
              </div>
              <div className="benchmark-item">
                <div className="benchmark-label">
                  <span className="benchmark-name">Needs Attention</span>
                  <span className="benchmark-range">&lt;20%</span>
                </div>
                <div className="benchmark-bar">
                  <div className="benchmark-fill poor" style={{ width: '25%' }}></div>
                </div>
              </div>
            </div>
            <div className="benchmark-current">
              <span className="current-indicator" style={{ background: statusInfo.color }}></span>
              <span className="current-text">
                Your rate: <strong style={{ color: statusInfo.color }}>{completionRate || 0}%</strong>
              </span>
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}

export default CompletionRate
