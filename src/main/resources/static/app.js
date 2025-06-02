// 全局变量
let currentUser = null
let currentPage = "login"
let currentSection = ""
let practiceQuestions = []
let currentQuestionIndex = 0

// API基础URL
const API_BASE_URL = "http://localhost:8088"

// 页面初始化
document.addEventListener("DOMContentLoaded", () => {
  // 添加样式以适配 Markdown 内容到聊天气泡
  const style = document.createElement("style")
  style.textContent = `
.message .message-content {
  max-width: 90%%;
  word-wrap: break-word;
  overflow-wrap: break-word;
  overflow-x: auto;
}
.message .message-content pre {
  background-color: #f4f4f4;
  padding: 8px;
  border-radius: 4px;
  overflow-x: auto;
  white-space: pre-wrap;
  word-break: break-word;
  max-width: 100%%;
}
.message .message-content code {
  background-color: #f0f0f0;
  padding: 2px 4px;
  border-radius: 4px;
}
`
  document.head.appendChild(style)

  initializeApp()
  setupEventListeners()
})

// 初始化应用
function initializeApp() {
  showPage("loginPage")

  // 检查是否有保存的登录状态
  const savedUser = localStorage.getItem("currentUser")
  if (savedUser) {
    currentUser = JSON.parse(savedUser)
    showUserPage()
  }
}

// 设置事件监听器
function setupEventListeners() {
  // 登录表单
  document.getElementById("loginForm").addEventListener("submit", handleLogin)

  // 导航菜单点击
  document.addEventListener("click", (e) => {
    if (e.target.matches("[data-section]")) {
      e.preventDefault()
      const section = e.target.getAttribute("data-section")
      showSection(section)
    }

    if (e.target.matches("[data-tab]")) {
      e.preventDefault()
      const tab = e.target.getAttribute("data-tab")
      showTab(e.target, tab)
    }
  })

  // 文件上传
  const uploadArea = document.getElementById("uploadArea")
  const fileInput = document.getElementById("fileInput")

  if (uploadArea && fileInput) {
    uploadArea.addEventListener("click", () => fileInput.click())
    uploadArea.addEventListener("dragover", handleDragOver)
    uploadArea.addEventListener("drop", handleFileDrop)
    fileInput.addEventListener("change", handleFileSelect)
  }

  // 模态框关闭
  window.addEventListener("click", (e) => {
    const modal = document.getElementById("modal")
    if (e.target === modal) {
      closeModal()
    }
  })
}

// 登录处理
async function handleLogin(e) {
  e.preventDefault()

  const username = document.getElementById("username").value
  const password = document.getElementById("password").value
  const role = document.getElementById("role").value

  if (!username || !password || !role) {
    showNotification("请填写完整的登录信息", "error")
    return
  }

  showLoading(true)

  try {
    const response = await fetch(
        `${API_BASE_URL}/admin/login?username=${encodeURIComponent(username)}&password=${encodeURIComponent(password)}&role=${encodeURIComponent(role)}`,
    )

    if (response) {
      console.log("登录响应:", response)
      const tmp = await response.json()
      const userData = tmp.data
      console.log("用户信息", userData)
      if (userData) {
        currentUser = userData
        localStorage.setItem("currentUser", JSON.stringify(userData))
        showNotification("登录成功！", "success")
        showUserPage()
      } else {
        showNotification("登录失败，请检查用户名和密码", "error")
      }
    } else {
      showNotification("登录失败，请检查用户名和密码", "error")
    }
  } catch (error) {
    console.error("登录错误:", error)
    showNotification("网络错误，请稍后重试", "error")
  } finally {
    showLoading(false)
  }
}

// 显示用户对应的页面
function showUserPage() {
  if (!currentUser) return

  switch (currentUser.role) {
    case "admin":
      showPage("adminPage")
      showSection("admin-dashboard")
      loadAdminData()
      break
    case "teacher":
      showPage("teacherPage")
      showSection("teacher-courses")
      loadTeacherData()
      break
    case "student":
      showPage("studentPage")
      showSection("student-ask")
      loadStudentData()
      break
    default:
      showNotification("未知用户角色", "error")
      logout()
  }
}

// 显示页面
function showPage(pageId) {
  document.querySelectorAll(".page").forEach((page) => {
    page.classList.remove("active")
  })
  document.getElementById(pageId).classList.add("active")
  currentPage = pageId
}

// 显示内容区域
function showSection(sectionId) {
  const currentPageElement = document.querySelector(".page.active")
  if (!currentPageElement) return

  currentPageElement.querySelectorAll(".content-section").forEach((section) => {
    section.classList.remove("active")
  })

  const targetSection = document.getElementById(sectionId)
  if (targetSection) {
    targetSection.classList.add("active")
    currentSection = sectionId

    // 更新导航菜单状态
    currentPageElement.querySelectorAll(".nav-menu a").forEach((link) => {
      link.classList.remove("active")
    })

    const activeLink = currentPageElement.querySelector(`[data-section="${sectionId}"]`)
    if (activeLink) {
      activeLink.classList.add("active")
    }
  }
}

// 显示标签页
function showTab(tabButton, tabId) {
  const tabContainer = tabButton.closest(".analytics-tabs, .history-tabs")
  const contentContainer = tabContainer.nextElementSibling

  // 更新按钮状态
  tabContainer.querySelectorAll(".tab-btn").forEach((btn) => {
    btn.classList.remove("active")
  })
  tabButton.classList.add("active")

  // 更新内容状态
  contentContainer.querySelectorAll(".tab-content").forEach((content) => {
    content.classList.remove("active")
  })

  const targetContent = document.getElementById(tabId)
  if (targetContent) {
    targetContent.classList.add("active")
  }
}

// 加载管理员数据
async function loadAdminData() {
  try {
    await Promise.all([loadUsers(), loadResources(), loadDashboardStats(), loadAnalyticsData()])
  } catch (error) {
    console.error("加载管理员数据失败:", error)
    showNotification("数据加载失败", "error")
  }
}

// 加载用户列表
async function loadUsers() {
  try {
    const response = await fetch(`${API_BASE_URL}/admin/viewUsers`)
    if (response.ok) {
      const result = await response.json()
      const users = result.data || result
      renderUsersTable(users)
      // 移除这行，因为统计数据现在通过 getCount 接口获取
      // updateUserCount(users.length)
    }
  } catch (error) {
    console.error("加载用户列表失败:", error)
  }
}

// 渲染用户表格
function renderUsersTable(users) {
  const tbody = document.querySelector("#usersTable tbody")
  if (!tbody) return

  tbody.innerHTML = ""

  users.forEach((user) => {
    const row = document.createElement("tr")
    row.innerHTML = `
            <td>${user.userId}</td>
            <td>${user.username}</td>
            <td><span class="badge badge-${getRoleBadgeClass(user.role)}">${getRoleDisplayName(user.role)}</span></td>
            <td>${user.email || "-"}</td>
            <td>${user.realName || "-"}</td>
            <td>${formatDate(user.createTime)}</td>
            <td class="action-buttons">
                <button class="btn btn-secondary btn-sm" onclick="editUser('${user.username}')">
                    <i class="fas fa-edit"></i>
                </button>
                <button class="btn btn-danger btn-sm" onclick="deleteUser('${user.username}')">
                    <i class="fas fa-trash"></i>
                </button>
            </td>
        `
    tbody.appendChild(row)
  })
}

// 加载资源列表
async function loadResources() {
  try {
    const response = await fetch(`${API_BASE_URL}/admin/viewResources`)
    if (response.ok) {
      const result = await response.json()
      const resources = result.data || result
      renderResourcesTable(resources)
    }
  } catch (error) {
    console.error("加载资源列表失败:", error)
  }
}

// 渲染资源表格
function renderResourcesTable(resources) {
  const tbody = document.querySelector("#resourcesTable tbody")
  if (!tbody) return

  tbody.innerHTML = ""

  resources.forEach((resource) => {
    const row = document.createElement("tr")
    row.innerHTML = `
            <td>${resource.materialId}</td>
            <td>${resource.title}</td>
            <td><span class="badge badge-info">${resource.materialType}</span></td>
            <td>${formatDate(resource.createTime)}</td>
            <td class="action-buttons">
                <button class="btn btn-primary btn-sm" onclick="exportResource(${resource.materialId})">
                    <i class="fas fa-download"></i>
                </button>
            </td>
        `
    tbody.appendChild(row)
  })
}

// 加载仪表板统计数据
async function loadDashboardStats() {
  try {
    // 使用新的统计接口
    const response = await fetch(`${API_BASE_URL}/admin/getCount`)
    if (response.ok) {
      const result = await response.json()
      const countData = result.data || result

      // 更新统计卡片
      updateStatCards(countData)

      // 继续加载使用情况数据
      const [teacherUsage, studentUsage] = await Promise.all([
        fetch(`${API_BASE_URL}/admin/teacherUsageByDay`)
            .then((r) => r.json())
            .then((result) => result.data || result),
        fetch(`${API_BASE_URL}/admin/studentUsageByDay`)
            .then((r) => r.json())
            .then((result) => result.data || result),
      ])

      renderUsageCharts(teacherUsage, studentUsage)
    }
  } catch (error) {
    console.error("加载统计数据失败:", error)
  }
}

// 更新统计卡片数据
function updateStatCards(countData) {
  // 更新教师数量
  const teacherCountElement = document.getElementById("teacherCount")
  if (teacherCountElement) {
    teacherCountElement.textContent = countData.teacherCnt || 0
  }

  // 更新学生数量
  const studentCountElement = document.getElementById("studentCount")
  if (studentCountElement) {
    studentCountElement.textContent = countData.studentCnt || 0
  }

  // 更新课程数量
  const courseCountElement = document.getElementById("courseCount")
  if (courseCountElement) {
    courseCountElement.textContent = countData.courseCnt || 0
  }

  // 更新题目数量
  const questionCountElement = document.getElementById("questionCount")
  if (questionCountElement) {
    questionCountElement.textContent = countData.questionCnt || 0
  }

  // 添加新的统计信息到页面上的其他元素（如果存在）
  updateAdditionalStats(countData)
}

// 更新额外的统计信息
function updateAdditionalStats(countData) {
  // 更新教学设计数量
  const lessonPlanCountElement = document.getElementById("lessonPlanCount")
  if (lessonPlanCountElement) {
    lessonPlanCountElement.textContent = countData.lessonPlanCnt || 0
  }

  // 更新资料数量
  const materialCountElement = document.getElementById("materialCount")
  if (materialCountElement) {
    materialCountElement.textContent = countData.materialCnt || 0
  }

  // 更新练习记录数量
  const practiceRecordCountElement = document.getElementById("practiceRecordCount")
  if (practiceRecordCountElement) {
    practiceRecordCountElement.textContent = countData.practiceRecordCnt || 0
  }

  // 更新答案统计
  const answerCountElement = document.getElementById("answerCount")
  if (answerCountElement) {
    answerCountElement.textContent = countData.answerCnt || 0
  }

  const correctCountElement = document.getElementById("correctCount")
  if (correctCountElement) {
    correctCountElement.textContent = countData.correctCnt || 0
  }

  const incorrectCountElement = document.getElementById("incorrectCount")
  if (incorrectCountElement) {
    incorrectCountElement.textContent = countData.incorrectCnt || 0
  }

  // 计算正确率
  const correctRateElement = document.getElementById("correctRate")
  if (correctRateElement && countData.answerCnt > 0) {
    const rate = ((countData.correctCnt / countData.answerCnt) * 100).toFixed(1)
    correctRateElement.textContent = rate + "%"
  }
}

// 渲染使用情况图表
function renderUsageCharts(teacherData, studentData) {
  const teacherChart = document.getElementById("teacherUsageChart")
  const studentChart = document.getElementById("studentUsageChart")

  if (teacherChart) {
    teacherChart.innerHTML = `
            <div class="chart-placeholder">
                <i class="fas fa-chart-bar"></i>
                <p>教师使用次数: ${teacherData.reduce((sum, item) => sum + item.count, 0)}</p>
            </div>
        `
  }

  if (studentChart) {
    studentChart.innerHTML = `
            <div class="chart-placeholder">
                <i class="fas fa-chart-line"></i>
                <p>学生使用次数: ${studentData.reduce((sum, item) => sum + item.count, 0)}</p>
            </div>
        `
  }
}

// 加载分析数据
async function loadAnalyticsData() {
  try {
    const [studentAnalysis, teacherAnalysis, learningEffect] = await Promise.all([
      fetch(`${API_BASE_URL}/admin/getStudentAllData`)
          .then((r) => r.json())
          .then((result) => result.data || result),
      fetch(`${API_BASE_URL}/admin/teachEffect`)
          .then((r) => r.json())
          .then((result) => result.data || result),
      fetch(`${API_BASE_URL}/admin/studentLearningEffect`)
          .then((r) => r.json())
          .then((result) => result.data || result),
    ])

    renderAnalyticsTables(studentAnalysis, teacherAnalysis, learningEffect)
  } catch (error) {
    console.error("加载分析数据失败:", error)
  }
}

// 渲染分析表格
function renderAnalyticsTables(studentData, teacherData, learningData) {
  // 学生分析表格
  const studentTbody = document.querySelector("#studentAnalysisTable tbody")
  if (studentTbody) {
    studentTbody.innerHTML = ""
    studentData.forEach((item) => {
      const row = document.createElement("tr")
      row.innerHTML = `
                <td>${item.studentId}</td>
                <td>${item.knowledgePoint}</td>
                <td>${item.avgCorrectRate.toFixed(2)}%</td>
                <td>${item.commonErrors || "-"}</td>
                <td>${item.teachingSuggestions || "-"}</td>
            `
      studentTbody.appendChild(row)
    })
  }

  // 教师分析表格
  const teacherTbody = document.querySelector("#teacherAnalysisTable tbody")
  if (teacherTbody) {
    teacherTbody.innerHTML = ""
    teacherData.forEach((item) => {
      const row = document.createElement("tr")
      row.innerHTML = `
                <td>${item.teacherId}</td>
                <td>${formatTime(item.avgPrepTime)}</td>
                <td>${formatTime(item.avgCorrectionTime)}</td>
                <td>${item.avgCorrectRate.toFixed(2)}%</td>
                <td>${item.highFreqErrorPoint || "-"}</td>
                <td>${item.optimizationSuggestion || "-"}</td>
            `
      teacherTbody.appendChild(row)
    })
  }

  // 学习效果表格
  const learningTbody = document.querySelector("#learningEffectTable tbody")
  if (learningTbody) {
    learningTbody.innerHTML = ""
    learningData.forEach((item) => {
      const row = document.createElement("tr")
      row.innerHTML = `
                <td>${item.studentId}</td>
                <td>${item.knowledgePoint}</td>
                <td>${item.avgCorrectRate.toFixed(2)}%</td>
                <td>${item.commonErrors || "-"}</td>
                <td>${item.teachingSuggestions || "-"}</td>
            `
      learningTbody.appendChild(row)
    })
  }
}

// 加载教师数据
async function loadTeacherData() {
  try {
    await Promise.all([loadCourses(), loadLessonPlans(), loadQuestions(), loadStudentsForAnalysis(), loadMaterials()])
  } catch (error) {
    console.error("加载教师数据失败:", error)
    showNotification("数据加载失败", "error")
  }
}

// 加载课程列表
async function loadCourses() {
  try {
    const response = await fetch(`${API_BASE_URL}/teacher/getAllCourses`)
    if (response.ok) {
      const result = await response.json()
      const courses = result.data || result
      renderCoursesGrid(courses)
      populateCourseSelects(courses)
    }
  } catch (error) {
    console.error("加载课程列表失败:", error)
  }
}

// 渲染课程网格
function renderCoursesGrid(courses) {
  const grid = document.getElementById("coursesGrid")
  if (!grid) return

  grid.innerHTML = ""

  if (courses.length === 0) {
    grid.innerHTML = `
            <div class="empty-state">
                <i class="fas fa-book"></i>
                <h3>暂无课程</h3>
                <p>点击"创建课程"按钮开始创建您的第一个课程</p>
            </div>
        `
    return
  }

  courses.forEach((course) => {
    const card = document.createElement("div")
    card.className = "course-card card-hover"
    card.innerHTML = `
            <h3>${course.courseName}</h3>
            <p><strong>学科:</strong> ${course.discipline}</p>
            <p>${course.description || "暂无描述"}</p>
            <div class="card-actions">
                <button class="btn btn-primary btn-sm" onclick="viewCourse(${course.courseId})">
                    <i class="fas fa-eye"></i> 查看
                </button>
                <button class="btn btn-secondary btn-sm" onclick="editCourse(${course.courseId})">
                    <i class="fas fa-edit"></i> 编辑
                </button>
                <button class="btn btn-danger btn-sm" onclick="deleteCourse('${course.courseName}')">
                    <i class="fas fa-trash"></i> 删除
                </button>
            </div>
        `
    grid.appendChild(card)
  })
}

// 填充课程选择框
function populateCourseSelects(courses) {
  const selects = document.querySelectorAll("#analyticsCourse, #questionCourse, #practiceCourse")
  selects.forEach((select) => {
    select.innerHTML = '<option value="">请选择课程</option>'
    courses.forEach((course) => {
      const option = document.createElement("option")
      option.value = course.courseId
      option.textContent = course.courseName
      select.appendChild(option)
    })
  })
}

// 加载教学设计
async function loadLessonPlans() {
  try {
    const response = await fetch(`${API_BASE_URL}/teacher/getAllLessonPlans`)
    if (response.ok) {
      const result = await response.json()
      const lessonPlans = result.data || result
      renderLessonPlansGrid(lessonPlans)
      populateLessonPlanSelect(lessonPlans)
    }
  } catch (error) {
    console.error("加载教学设计失败:", error)
  }
}

// 渲染教学设计网格
function renderLessonPlansGrid(lessonPlans) {
  const grid = document.getElementById("lessonsGrid")
  if (!grid) return

  grid.innerHTML = ""

  if (lessonPlans.length === 0) {
    grid.innerHTML = `
            <div class="empty-state">
                <i class="fas fa-clipboard-list"></i>
                <h3>暂无教学设计</h3>
                <p>点击"创建教学设计"按钮开始创建</p>
            </div>
        `
    return
  }

  lessonPlans.forEach((lesson) => {
    const card = document.createElement("div")
    card.className = "lesson-card card-hover"
    card.innerHTML = `
            <h3>${lesson.title + lesson.lessonPlanId}</h3>
            <p class="text-truncate-3">${truncateMarkdown(lesson.content)}</p>
            <div class="card-actions">
                <button class="btn btn-primary btn-sm" onclick="viewLesson(${lesson.lessonPlanId})">
                    <i class="fas fa-eye"></i> 查看
                </button>
                <button class="btn btn-secondary btn-sm" onclick="editLesson(${lesson.lessonPlanId})">
                    <i class="fas fa-edit"></i> 编辑
                </button>
                <button class="btn btn-success btn-sm" onclick="exportResource(${lesson.lessonPlanId})">
                    <i class="fas fa-download"></i> 导出
                </button>
                <button class="btn btn-danger btn-sm" onclick="deleteLessonPlan(${lesson.lessonPlanId})">
                    <i class="fas fa-trash"></i> 删除
                </button>
            </div>
        `
    grid.appendChild(card)
  })
}

// 填充教学设计选择框
function populateLessonPlanSelect(lessonPlans) {
  const select = document.getElementById("lessonPlanFilter")
  if (select) {
    select.innerHTML = '<option value="">所有教学设计</option>'
    lessonPlans.forEach((lesson) => {
      const option = document.createElement("option")
      option.value = lesson.lessonPlanId
      option.textContent = lesson.title
      select.appendChild(option)
    })
  }
}

// 加载题目列表
async function loadQuestions() {
  try {
    const response = await fetch(`${API_BASE_URL}/teacher/getAllQuestions`)
    if (response.ok) {
      const result = await response.json()
      const questions = result.data || result
      renderQuestionsGrid(questions)
    }
  } catch (error) {
    console.error("加载题目列表失败:", error)
  }
}

// 题目过滤（本地过滤版本）
async function filterQuestions() {
  const lessonPlanFilter = document.getElementById("lessonPlanFilter").value
  const questionTypeFilter = document.getElementById("questionTypeFilter").value

  showLoading(true)
  try {
    // 加载所有题目
    const response = await fetch(`${API_BASE_URL}/teacher/getAllQuestions`)
    if (response.ok) {
      const result = await response.json()
      let questions = result.data || result

      // 应用过滤
      if (lessonPlanFilter) {
        questions = questions.filter((q) => q.lessonPlanId == lessonPlanFilter)
      }
      if (questionTypeFilter) {
        questions = questions.filter((q) => q.questionType === questionTypeFilter)
      }

      renderQuestionsGrid(questions)
    } else {
      showNotification("加载题目列表失败", "error")
    }
  } catch (error) {
    console.error("加载题目列表失败:", error)
    showNotification("网络错误，请稍后重试", "error")
  } finally {
    showLoading(false)
  }
}

// 渲染题目网格
function renderQuestionsGrid(questions) {
  const grid = document.getElementById("questionsGrid")
  if (!grid) return

  grid.innerHTML = ""

  if (questions.length === 0) {
    grid.innerHTML = `
            <div class="empty-state">
                <i class="fas fa-question-circle"></i>
                <h3>暂无题目</h3>
                <p>点击"生成题目"按钮开始创建题目</p>
            </div>
        `
    return
  }

  questions.forEach((question) => {
    const card = document.createElement("div")
    card.className = "question-card card-hover"
    card.innerHTML = `
            <h3>题目 #${question.questionId}</h3>
            <p><strong>类型:</strong> <span class="badge badge-${getQuestionTypeBadgeClass(question.questionType)}">${getQuestionTypeDisplayName(question.questionType)}</span></p>
            <p><strong>知识点:</strong> ${question.knowledgePoint}</p>
            <p class="text-truncate-3">${question.questionText}</p>
            <div class="card-actions">
                <button class="btn btn-primary btn-sm" onclick="viewQuestion(${question.questionId})">
                    <i class="fas fa-eye"></i> 查看
                </button>
                <button class="btn btn-secondary btn-sm" onclick="editQuestion(${question.questionId})">
                    <i class="fas fa-edit"></i> 编辑
                </button>
                <button class="btn btn-danger btn-sm" onclick="deleteQuestion(${question.questionId})">
                    <i class="fas fa-trash"></i> 删除
                </button>
            </div>
        `
    grid.appendChild(card)
  })
}

// 加载学生数据
async function loadStudentData() {
  try {
    await Promise.all([loadCourses(), loadQuestionHistory(), loadPracticeHistory()])
  } catch (error) {
    console.error("加载学生数据失败:", error)
    showNotification("数据加载失败", "error")
  }
}

// 加载提问历史
async function loadQuestionHistory() {
  try {
    const response = await fetch(`${API_BASE_URL}/student/getAllQuestionsByStudentId`)
    if (response.ok) {
      const result = await response.json()
      const history = result.data || result
      renderQuestionHistory(history)
    }
  } catch (error) {
    console.error("加载提问历史失败:", error)
  }
}

// 渲染提问历史
function renderQuestionHistory(history) {
  const container = document.getElementById("questionHistoryList")
  if (!container) return

  container.innerHTML = ""

  if (history.length === 0) {
    container.innerHTML = `
            <div class="empty-state">
                <i class="fas fa-comments"></i>
                <h3>暂无提问记录</h3>
                <p>开始向AI助手提问吧！</p>
            </div>
        `
    return
  }

  history.forEach((item) => {
    const historyItem = document.createElement("div")
    historyItem.className = "history-item"
    historyItem.innerHTML = `
            <h4>问题: ${item.questionText}</h4>
            <p><strong>回答:</strong> ${item.answerText}</p>
            <p class="time">${formatDate(item.createTime)}</p>
        `
    container.appendChild(historyItem)
  })
}

// 学生提问
async function askQuestion() {
  const courseSelect = document.getElementById("questionCourse")
  const questionInput = document.getElementById("questionInput")

  const courseId = courseSelect.value
  const questionText = questionInput.value.trim()

  if (!courseId) {
    showNotification("请选择课程", "warning")
    return
  }

  if (!questionText) {
    showNotification("请输入问题", "warning")
    return
  }

  // 添加用户消息到聊天界面
  addMessageToChat(questionText, "user")
  questionInput.value = ""

  showLoading(true)

  try {
    const response = await fetch(
        `${API_BASE_URL}/student/askQuestion?courseId=${courseId}&questionText=${encodeURIComponent(questionText)}`,
    )

    console.log("响应数据:", response)
    if (response) {
      const tmp = await response.json()
      const answer = tmp.data
      addMessageToChat(answer, "assistant")
      showNotification("问题提交成功", "success")
    } else {
      showNotification("提问失败，请稍后重试", "error")
    }
  } catch (error) {
    console.error("提问失败:", error)
    showNotification("网络错误，请稍后重试", "error")
  } finally {
    showLoading(false)
  }
}

// 添加消息到聊天界面

function addMessageToChat(message, sender) {
  const chatMessages = document.getElementById("chatMessages")
  if (!chatMessages) return

  const messageDiv = document.createElement("div")
  messageDiv.className = `message ${sender}`

  // 使用 marked.js 渲染 AI 回复的 Markdown 内容
  const parsedMessage = sender === "assistant" ? marked.parse(message) : escapeHtml(message)

  messageDiv.innerHTML = `
        <div class="message-content">
            ${parsedMessage}
        </div>
    `

  chatMessages.appendChild(messageDiv)
  chatMessages.scrollTop = chatMessages.scrollHeight
}

// 生成练习题目
async function generatePractice() {
  const courseSelect = document.getElementById("practiceCourse")
  const knowledgePointInput = document.getElementById("knowledgePoint")
  const quantityInput = document.getElementById("questionQuantity")

  const courseId = courseSelect.value
  const knowledgePoint = knowledgePointInput.value.trim()
  const quantity = Number.parseInt(quantityInput.value)

  if (!courseId) {
    showNotification("请选择课程", "warning")
    return
  }

  if (!knowledgePoint) {
    showNotification("请输入知识点", "warning")
    return
  }

  if (!quantity || quantity < 1 || quantity > 10) {
    showNotification("题目数量应在1-10之间", "warning")
    return
  }

  showLoading(true)

  try {
    const response = await fetch(
        `${API_BASE_URL}/student/getPracticeQuestions?courseId=${courseId}&knowledgePoint=${encodeURIComponent(knowledgePoint)}&quantity=${quantity}`,
    )

    if (response.ok) {
      const result = await response.json()
      practiceQuestions = result.data || result
      if (practiceQuestions && practiceQuestions.length > 0) {
        currentQuestionIndex = 0
        showPracticeQuestions()
        showNotification("练习题目生成成功", "success")
      } else {
        showNotification("生成题目失败，请稍后重试", "error")
      }
    } else {
      showNotification("生成题目失败，请稍后重试", "error")
    }
  } catch (error) {
    console.error("生成练习失败:", error)
    showNotification("网络错误，请稍后重试", "error")
  } finally {
    showLoading(false)
  }
}

// 显示练习题目
function showPracticeQuestions() {
  document.getElementById("practiceSetup").style.display = "none"
  document.getElementById("practiceQuestions").style.display = "block"
  document.getElementById("practiceResult").style.display = "none"

  updateQuestionDisplay()
}

// 更新题目显示
function updateQuestionDisplay() {
  if (!practiceQuestions || practiceQuestions.length === 0) return

  const currentQuestion = practiceQuestions[currentQuestionIndex]

  document.getElementById("currentQuestion").textContent = currentQuestionIndex + 1
  document.getElementById("totalQuestions").textContent = practiceQuestions.length
  document.getElementById("questionContent").innerHTML = `
        <h4>题目类型: ${getQuestionTypeDisplayName(currentQuestion.questionType)}</h4>
        <p><strong>知识点:</strong> ${currentQuestion.knowledgePoint}</p>
        <div class="question-text">${currentQuestion.questionText}</div>
    `

  // 更新按钮状态
  const prevBtn = document.querySelector('[onclick="previousQuestion()"]')
  const nextBtn = document.querySelector('[onclick="nextQuestion()"]')
  const submitBtn = document.querySelector('[onclick="submitPractice()"]')

  if (prevBtn) prevBtn.style.display = currentQuestionIndex > 0 ? "inline-block" : "none"
  if (nextBtn) nextBtn.style.display = currentQuestionIndex < practiceQuestions.length - 1 ? "inline-block" : "none"
  if (submitBtn)
    submitBtn.style.display = currentQuestionIndex === practiceQuestions.length - 1 ? "inline-block" : "none"
}

// 上一题
function previousQuestion() {
  if (currentQuestionIndex > 0) {
    currentQuestionIndex--
    updateQuestionDisplay()
  }
}

// 下一题
function nextQuestion() {
  if (currentQuestionIndex < practiceQuestions.length - 1) {
    currentQuestionIndex++
    updateQuestionDisplay()
  }
}

// 提交练习
async function submitPractice() {
  const answerInput = document.getElementById("answerInput")
  const answer = answerInput.value.trim()

  if (!answer) {
    showNotification("请输入答案", "warning")
    return
  }

  const currentQuestion = practiceQuestions[currentQuestionIndex]

  showLoading(true)

  try {
    const response = await fetch(
        `${API_BASE_URL}/student/submitPractice?questionId=${currentQuestion.questionId}&submittedAnswer=${encodeURIComponent(answer)}`,
    )

    if (response.ok) {
      const responseData = await response.json()
      const result = responseData.data || responseData
      showPracticeResult(result)
      showNotification("答案提交成功", "success")
    } else {
      showNotification("提交失败，请稍后重试", "error")
    }
  } catch (error) {
    console.error("提交练习失败:", error)
    showNotification("网络错误，请稍后重试", "error")
  } finally {
    showLoading(false)
  }
}

// 显示练习结果
function showPracticeResult(result) {
  document.getElementById("practiceQuestions").style.display = "none"
  const resultDiv = document.getElementById("practiceResult")
  resultDiv.style.display = "block"

  resultDiv.innerHTML = `
        <div class="result-card">
            <h3>练习结果</h3>
            <div class="result-status ${result.isCorrect ? "correct" : "incorrect"}">
                <i class="fas fa-${result.isCorrect ? "check-circle" : "times-circle"}"></i>
                <span>${result.isCorrect ? "回答正确" : "回答错误"}</span>
            </div>
            <div class="result-details">
                <h4>您的答案:</h4>
                <p>${result.submittedAnswer}</p>
                ${
      result.errorAnalysis
          ? `
                    <h4>分析建议:</h4>
                    <p>${result.errorAnalysis}</p>
                `
          : ""
  }
            </div>
            <div class="result-actions">
                <button class="btn btn-primary" onclick="resetPractice()">重新练习</button>
                <button class="btn btn-secondary" onclick="backToPracticeSetup()">返回设置</button>
            </div>
        </div>
    `
}

// 重置练习
function resetPractice() {
  practiceQuestions = []
  currentQuestionIndex = 0
  document.getElementById("answerInput").value = ""
  backToPracticeSetup()
}

// 返回练习设置
function backToPracticeSetup() {
  document.getElementById("practiceSetup").style.display = "block"
  document.getElementById("practiceQuestions").style.display = "none"
  document.getElementById("practiceResult").style.display = "none"
}

// 文件上传处理
function handleDragOver(e) {
  e.preventDefault()
  e.currentTarget.classList.add("drag-over")
}

function handleFileDrop(e) {
  e.preventDefault()
  e.currentTarget.classList.remove("drag-over")

  const files = e.dataTransfer.files
  if (files.length > 0) {
    uploadFile(files[0])
  }
}

function handleFileSelect(e) {
  const files = e.target.files
  if (files.length > 0) {
    uploadFile(files[0])
  }
}

// 上传文件
async function uploadFile(file) {
  const allowedTypes = [".pdf", ".doc", ".docx", ".txt"]
  const fileExtension = "." + file.name.split(".").pop().toLowerCase()

  if (!allowedTypes.includes(fileExtension)) {
    showNotification("只支持PDF、DOC、DOCX、TXT格式的文件", "error")
    return
  }

  const formData = new FormData()
  formData.append("file", file)

  const progressDiv = document.getElementById("uploadProgress")
  const progressFill = document.getElementById("progressFill")
  const progressText = document.getElementById("progressText")

  progressDiv.style.display = "block"
  progressFill.style.width = "0%"
  progressText.textContent = "上传中..."

  try {
    const xhr = new XMLHttpRequest()

    xhr.upload.addEventListener("progress", (e) => {
      if (e.lengthComputable) {
        const percentComplete = (e.loaded / e.total) * 100
        progressFill.style.width = percentComplete + "%"
        progressText.textContent = `上传中... ${Math.round(percentComplete)}%`
      }
    })

    xhr.addEventListener("load", () => {
      if (xhr.status === 200) {
        progressText.textContent = "上传成功！"
        showNotification("文件上传成功", "success")
        setTimeout(() => {
          progressDiv.style.display = "none"
        }, 2000)
      } else {
        showNotification("上传失败，请稍后重试", "error")
        progressDiv.style.display = "none"
      }
    })

    xhr.addEventListener("error", () => {
      showNotification("上传失败，请稍后重试", "error")
      progressDiv.style.display = "none"
    })

    xhr.open("POST", `${API_BASE_URL}/teacher/uploadFile`)
    xhr.send(formData)
  } catch (error) {
    console.error("文件上传失败:", error)
    showNotification("上传失败，请稍后重试", "error")
    progressDiv.style.display = "none"
  }
}

// 模态框相关函数
function showModal(title, content) {
  const modal = document.getElementById("modal")
  const modalBody = document.getElementById("modalBody")

  modalBody.innerHTML = `
        <div class="modal-header">
            <h3>${title}</h3>
        </div>
        <div class="modal-body">
            ${content}
        </div>
    `

  modal.style.display = "block"
  setTimeout(() => modal.classList.add("show"), 10)
}

function closeModal() {
  const modal = document.getElementById("modal")
  modal.classList.remove("show")
  setTimeout(() => (modal.style.display = "none"), 300)
}

// 显示创建课程模态框
function showCreateCourseModal() {
  const content = `
        <form id="createCourseForm">
            <div class="form-group">
                <label for="courseName">课程名称</label>
                <input type="text" id="courseName" name="courseName" required>
            </div>
            <div class="form-group">
                <label for="discipline">学科</label>
                <input type="text" id="discipline" name="discipline" required>
            </div>
            <div class="form-group">
                <label for="description">课程描述</label>
                <textarea id="description" name="description" rows="4"></textarea>
            </div>
            <div class="form-actions">
                <button type="button" class="btn btn-secondary" onclick="closeModal()">取消</button>
                <button type="submit" class="btn btn-primary">创建课程</button>
            </div>
        </form>
    `

  showModal("创建课程", content)

  document.getElementById("createCourseForm").addEventListener("submit", async (e) => {
    e.preventDefault()

    const formData = new FormData(e.target)
    const courseName = formData.get("courseName")
    const discipline = formData.get("discipline")
    const description = formData.get("description")

    try {
      const response = await fetch(
          `${API_BASE_URL}/teacher/createCourse?courseName=${encodeURIComponent(courseName)}&discipline=${encodeURIComponent(discipline)}&description=${encodeURIComponent(description)}`,
      )

      if (response.ok) {
        showNotification("课程创建成功", "success")
        closeModal()
        loadCourses()
      } else {
        showNotification("创建失败，请稍后重试", "error")
      }
    } catch (error) {
      console.error("创建课程失败:", error)
      showNotification("网络错误，请稍后重试", "error")
    }
  })
}

// 显示创建教学设计模态框
function showCreateLessonModal() {
  const content = `
    <form id="createLessonForm">
      <div class="form-group">
        <label for="lessonCourseName">课程名称</label>
        <select class="form-control" id="lessonCourseName" name="courseId" required>
          <option value="">请选择课程</option>
        </select>
      </div>
      <div class="form-group">
        <label for="lessonQuestion">教学要求</label>
        <textarea id="lessonQuestion" name="question" rows="4" placeholder="请描述您的教学设计要求..." required></textarea>
      </div>
      <div class="form-actions">
        <button type="button" class="btn btn-secondary" onclick="closeModal()">取消</button>
        <button type="submit" class="btn btn-primary">生成教学设计</button>
      </div>
    </form>
  `

  showModal("创建教学设计", content)

  // 动态加载课程数据
  loadCoursesForLessonModal()

  document.getElementById("createLessonForm").addEventListener("submit", async (e) => {
    e.preventDefault()

    const formData = new FormData(e.target)
    const courseId = formData.get("courseId") // 修改为 courseId
    const question = formData.get("question")

    showLoading(true)

    try {
      const response = await fetch(
          `${API_BASE_URL}/teacher/createLessonPlan?courseId=${encodeURIComponent(courseId)}&question=${encodeURIComponent(question)}`,
      )
      if (response.ok) {
        showNotification("教学设计创建成功", "success")
        closeModal()
        loadLessonPlans()
      } else {
        showNotification("创建失败，请稍后重试", "error")
      }
    } catch (error) {
      console.error("创建教学设计失败:", error)
      showNotification("网络错误，请稍后重试", "error")
    } finally {
      showLoading(false)
    }
  })
}

// 为模态框加载课程列表
async function loadCoursesForLessonModal() {
  try {
    const response = await fetch(`${API_BASE_URL}/teacher/getAllCourses`)
    if (response.ok) {
      const result = await response.json()
      const courses = result.data || result
      const select = document.getElementById("lessonCourseName")
      if (select) {
        select.innerHTML = '<option value="">请选择课程</option>' // 清空并保留默认选项
        courses.forEach((course) => {
          const option = document.createElement("option")
          option.value = course.courseId // 使用 courseId 作为 value
          option.textContent = course.courseName // 显示 courseName
          select.appendChild(option)
        })
      }
    } else {
      showNotification("获取课程列表失败", "error")
    }
  } catch (error) {
    console.error("加载课程列表失败:", error)
    showNotification("网络错误，请稍后重试", "error")
  }
}

// 显示生成题目模态框
function showGenerateQuestionsModal() {
  const content = `
        <form id="generateQuestionsForm">
            <div class="form-group">
                <label for="questionLessonPlan">教学设计</label>
                <select id="questionLessonPlan" name="lessonPlanId" required>
                    <option value="">请选择教学设计</option>
                </select>
            </div>
            <div class="form-group">
                <label for="questionType">题目类型</label>
                <select id="questionType" name="questionType" required>
                    <option value="">请选择题目类型</option>
                    <option value="multiple_choice">选择题</option>
                    <option value="programming">编程题</option>
                    <option value="short_answer">简答题</option>
                </select>
            </div>
            <div class="form-group">
                <label for="questionKnowledgePoint">知识点</label>
                <input type="text" id="questionKnowledgePoint" name="knowledgePoint" required>
            </div>
            <div class="form-group">
                <label for="questionQuantity">生成数量</label>
                <input type="number" id="questionQuantity" name="quantity" min="1" max="10" value="5" required>
            </div>
            <div class="form-actions">
                <button type="button" class="btn btn-secondary" onclick="closeModal()">取消</button>
                <button type="submit" class="btn btn-primary">生成题目</button>
            </div>
        </form>
    `

  showModal("生成题目", content)

  // 填充教学设计选项
  loadLessonPlansForModal()

  document.getElementById("generateQuestionsForm").addEventListener("submit", async (e) => {
    e.preventDefault()

    const formData = new FormData(e.target)
    const lessonPlanId = formData.get("lessonPlanId")
    const questionType = formData.get("questionType")
    const knowledgePoint = formData.get("knowledgePoint")
    const quantity = formData.get("quantity")

    showLoading(true)

    try {
      const response = await fetch(
          `${API_BASE_URL}/teacher/generateQuestions?lessonPlanId=${lessonPlanId}&questionType=${encodeURIComponent(questionType)}&knowledgePoint=${encodeURIComponent(knowledgePoint)}&quantity=${quantity}`,
      )

      if (response.ok) {
        showNotification("题目生成成功", "success")
        closeModal()
        loadQuestions()
      } else {
        showNotification("生成失败，请稍后重试", "error")
      }
    } catch (error) {
      console.error("生成题目失败:", error)
      showNotification("网络错误，请稍后重试", "error")
    }
  })
}

// 为模态框加载教学设计列表
async function loadLessonPlansForModal() {
  try {
    const response = await fetch(`${API_BASE_URL}/teacher/getAllLessonPlans`)
    if (response.ok) {
      const result = await response.json()
      const lessonPlans = result.data || result
      const select = document.getElementById("questionLessonPlan")
      if (select) {
        lessonPlans.forEach((lesson) => {
          const option = document.createElement("option")
          option.value = lesson.lessonPlanId
          option.textContent = lesson.title
          select.appendChild(option)
        })
      }
    }
  } catch (error) {
    console.error("加载教学设计失败:", error)
  }
}

// 显示添加用户模态框
function showAddUserModal() {
  const content = `
        <form id="addUserForm">
            <div class="form-group">
                <label for="newUsername">用户名</label>
                <input type="text" id="newUsername" name="username" required>
            </div>
            <div class="form-group">
                <label for="newPassword">密码</label>
                <input type="password" id="newPassword" name="password" required>
            </div>
            <div class="form-group">
                <label for="newRole">角色</label>
                <select id="newRole" name="role" required>
                    <option value="">请选择角色</option>
                    <option value="admin">管理员</option>
                    <option value="teacher">教师</option>
                    <option value="student">学生</option>
                </select>
            </div>
            <div class="form-group">
                <label for="newEmail">邮箱</label>
                <input type="email" id="newEmail" name="email">
            </div>
            <div class="form-group">
                <label for="newRealName">真实姓名</label>
                <input type="text" id="newRealName" name="realName">
            </div>
            <div class="form-group">
                <label for="newCode">编号</label>
                <input type="text" id="newCode" name="code">
            </div>
            <div class="form-actions">
                <button type="button" class="btn btn-secondary" onclick="closeModal()">取消</button>
                <button type="submit" class="btn btn-primary">添加用户</button>
            </div>
        </form>
    `

  showModal("添加用户", content)

  document.getElementById("addUserForm").addEventListener("submit", async (e) => {
    e.preventDefault()

    const formData = new FormData(e.target)
    const params = new URLSearchParams()

    for (const [key, value] of formData.entries()) {
      if (value) params.append(key, value)
    }

    try {
      const response = await fetch(`${API_BASE_URL}/admin/addUser?${params.toString()}`)

      if (response.ok) {
        showNotification("用户添加成功", "success")
        closeModal()
        loadUsers()
      } else {
        showNotification("添加失败，请稍后重试", "error")
      }
    } catch (error) {
      console.error("添加用户失败:", error)
      showNotification("网络错误，请稍后重试", "error")
    }
  })
}

// 生成学情分析
async function generateAnalysis() {
  const courseSelect = document.getElementById("analyticsCourse")
  const studentSelect = document.getElementById("analyticsStudent")

  const courseId = courseSelect.value
  const studentId = studentSelect.value

  if (!courseId || !studentId) {
    showNotification("请选择课程和学生", "warning")
    return
  }

  showLoading(true)

  try {
    const response = await fetch(
        `${API_BASE_URL}/teacher/viewLearningAnalysis?courseId=${courseId}&studentId=${studentId}`,
    )

    if (response) {
      const analysis = await response.json();
      console.log("学情分析结果", analysis);
      console.log("分析报告生成成功",analysis.data);
      const resultDiv = document.getElementById("analysisResult")
      resultDiv.innerHTML = `
                <h3>学情分析报告</h3>
                <div class="analysis-content">
                    ${analysis.data}
                </div>
            `
      showNotification("分析报告生成成功", "success")
    } else {
      showNotification("生成失败，请稍后重试", "error")
    }
  } catch (error) {
    console.error("生成分析失败:", error)
    showNotification("网络错误，请稍后重试", "error")
  } finally {
    showLoading(false)
  }
}

// 加载学生列表用于分析
async function loadStudentsForAnalysis() {
  try {
    const response = await fetch(`${API_BASE_URL}/admin/viewUsers`)
    if (response) {
      console.log("加载学生列表成功", response)
      const result = await response.json()
      const users = result.data || result
      console.log("用户列表", users)
      const students = users.filter((user) => user.role === "student")
      console.log("学生列表", students)

      const select = document.getElementById("analyticsStudent")
      if (select) {
        select.innerHTML = '<option value="">请选择学生</option>'
        students.forEach((student) => {
          const option = document.createElement("option")
          option.value = student.userId
          option.textContent = student.realName || student.username
          select.appendChild(option)
        })
      }
    }
  } catch (error) {
    console.error("加载学生列表失败:", error)
  }
}

// 导出资源
async function exportResource(lessonPlanId) {
  try {
    const response = await fetch(`${API_BASE_URL}/admin/exportResource?lessonPlanId=${lessonPlanId}`);

    if (!response.ok) {
      showNotification("导出失败，请稍后重试", "error");
      return;
    }

    const contentType = response.headers.get("content-type");
    if (!contentType || !contentType.includes("application/pdf")) {
      const text = await response.text(); // 或者 json()
      console.error("非 PDF 响应:", text);
      showNotification("导出失败，服务器返回错误", "error");
      return;
    }

    const blob = await response.blob();
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement("a");
    a.href = url;
    a.download = `lesson_plan_${lessonPlanId}.pdf`;
    document.body.appendChild(a);
    a.click();
    window.URL.revokeObjectURL(url);
    document.body.removeChild(a);
    showNotification("资源导出成功", "success");
  } catch (error) {
    console.error("导出资源失败:", error);
    showNotification("网络错误，请稍后重试", "error");
  }
}
// 删除用户
async function deleteUser(username) {
  if (!confirm("确定要删除这个用户吗？")) {
    return
  }

  try {
    const response = await fetch(`${API_BASE_URL}/admin/deleteUser?username=${encodeURIComponent(username)}`)

    if (response.ok) {
      showNotification("用户删除成功", "success")
      loadUsers()
    } else {
      showNotification("删除失败，请稍后重试", "error")
    }
  } catch (error) {
    console.error("删除用户失败:", error)
    showNotification("网络错误，请稍后重试", "error")
  }
}

// 退出登录
function logout() {
  currentUser = null
  localStorage.removeItem("currentUser")
  showPage("loginPage")
  showNotification("已退出登录", "info")
}

// 显示加载动画
function showLoading(show) {
  const loading = document.getElementById("loading")
  if (show) {
    loading.classList.add("show")
  } else {
    loading.classList.remove("show")
  }
}

// 显示通知
function showNotification(message, type = "info") {
  const notification = document.createElement("div")
  notification.className = `notification ${type}`
  notification.textContent = message

  document.body.appendChild(notification)

  setTimeout(() => notification.classList.add("show"), 100)

  setTimeout(() => {
    notification.classList.remove("show")
    setTimeout(() => document.body.removeChild(notification), 300)
  }, 3000)
}

// 工具函数
function formatDate(dateString) {
  if (!dateString) return "-"
  const date = new Date(dateString)
  return date.toLocaleString("zh-CN")
}

function formatTime(seconds) {
  if (!seconds) return "-"
  const hours = Math.floor(seconds / 3600)
  const minutes = Math.floor((seconds % 3600) / 60)
  return `${hours}小时${minutes}分钟`
}

function getRoleBadgeClass(role) {
  const classes = {
    admin: "danger",
    teacher: "primary",
    student: "success",
  }
  return classes[role] || "secondary"
}

function getRoleDisplayName(role) {
  const names = {
    admin: "管理员",
    teacher: "教师",
    student: "学生",
  }
  return names[role] || role
}

function getQuestionTypeBadgeClass(type) {
  const classes = {
    multiple_choice: "info",
    programming: "warning",
    short_answer: "success",
  }
  return classes[type] || "secondary"
}

function getQuestionTypeDisplayName(type) {
  const names = {
    multiple_choice: "选择题",
    programming: "编程题",
    short_answer: "简答题",
  }
  return names[type] || type
}

// 删除这个函数，因为已经被新的统计逻辑替代
// function updateUserCount(count) {
//   const element = document.getElementById("teacherCount")
//   if (element) {
//     element.textContent = count
//   }
// }

// 加载练习历史
async function loadPracticeHistory() {
  const container = document.getElementById("practiceHistoryList")
  if (!container) return

  showLoading(true)

  try {
    const response = await fetch(`${API_BASE_URL}/student/getPracticeHistory`, {
      method: "GET",
      headers: {
        "Content-Type": "application/json",
        // 如果需要认证头（如 token），可在此添加
        // 例如：Authorization: `Bearer ${localStorage.getItem("token")}`
      },
    })

    if (response) {
      const result = await response.json()
      const history = result.data || []
      renderPracticeHistory(history)
    } else {
      showNotification("加载练习历史失败", "error")
      container.innerHTML = `
        <div class="empty-state">
          <i class="fas fa-clipboard-check"></i>
          <h3>暂无练习记录</h3>
          <p>完成练习后记录会显示在这里</p>
        </div>
      `
    }
  } catch (error) {
    console.error("加载练习历史失败:", error)
    showNotification("网络错误，请稍后重试", "error")
    container.innerHTML = `
      <div class="empty-state">
        <i class="fas fa-clipboard-check"></i>
        <h3>暂无练习记录</h3>
        <p>完成练习后记录会显示在这里</p>
      </div>
    `
  } finally {
    showLoading(false)
  }
}

// 渲染练习历史
function renderPracticeHistory(history) {
  const container = document.getElementById("practiceHistoryList")
  if (!container) return

  container.innerHTML = ""

  if (!history || history.length === 0) {
    container.innerHTML = `
      <div class="empty-state">
        <i class="fas fa-clipboard-check"></i>
        <h3>暂无练习记录</h3>
        <p>完成练习后记录会显示在这里</p>
      </div>
    `
    return
  }

  history.forEach((item) => {
    const historyItem = document.createElement("div")
    historyItem.className = "history-item"
    historyItem.innerHTML = `
      <h4>题目 #${item.questionId}</h4>
      <p><strong>知识点:</strong> ${item.knowledgePoint || "未知"}</p>
      <p><strong>题目类型:</strong> ${getQuestionTypeDisplayName(item.questionType)}</p>
      <p><strong>题目内容:</strong> ${item.questionText || "无内容"}</p>
      <p><strong>您的答案:</strong> ${item.submittedAnswer || "未提交"}</p>
      <p><strong>结果:</strong> <span class="${item.isCorrect ? "text-success" : "text-danger"}">${item.isCorrect ? "正确" : "错误"}</span></p>
      ${item.errorAnalysis ? `<p><strong>分析建议:</strong> ${item.errorAnalysis}</p>` : ""}
      <p class="time">${formatDate(item.submitTime)}</p>
    `
    container.appendChild(historyItem)
  })
}

// 查看课程详情
async function viewCourse(courseId) {
  try {
    showLoading(true)
    const response = await fetch(`${API_BASE_URL}/teacher/getAllCourses`)
    if (response.ok) {
      const result = await response.json()
      const courses = result.data || result
      const course = courses.find((c) => c.courseId === courseId)

      if (course) {
        const content = `
          <div class="course-detail">
            <div class="detail-item">
              <label>课程ID:</label>
              <span>${course.courseId}</span>
            </div>
            <div class="detail-item">
              <label>课程名称:</label>
              <span>${course.courseName}</span>
            </div>
            <div class="detail-item">
              <label>学科:</label>
              <span>${course.discipline}</span>
            </div>
            <div class="detail-item">
              <label>课程描述:</label>
              <span>${course.description || "暂无描述"}</span>
            </div>
            <div class="detail-item">
              <label>创建时间:</label>
              <span>${formatDate(course.createTime)}</span>
            </div>
            <div class="detail-item">
              <label>更新时间:</label>
              <span>${formatDate(course.updatedTime)}</span>
            </div>
          </div>
          <style>
            .course-detail { padding: 20px; }
            .detail-item { display: flex; margin-bottom: 15px; }
            .detail-item label { min-width: 120px; font-weight: 600; color: #2c3e50; }
            .detail-item span { color: #6c757d; }
          </style>
        `
        showModal("课程详情", content)
      } else {
        showNotification("课程不存在", "error")
      }
    } else {
      showNotification("获取课程详情失败", "error")
    }
  } catch (error) {
    console.error("查看课程详情失败:", error)
    showNotification("网络错误，请稍后重试", "error")
  } finally {
    showLoading(false)
  }
}

// 编辑课程
async function editCourse(courseId) {
  try {
    showLoading(true)
    const response = await fetch(`${API_BASE_URL}/teacher/getAllCourses`)
    if (response.ok) {
      const result = await response.json()
      const courses = result.data || result
      const course = courses.find((c) => c.courseId === courseId)

      if (course) {
        const content = `
          <form id="editCourseForm">
            <input type="hidden" name="courseId" value="${course.courseId}">
            <div class="form-group">
              <label for="editCourseName">课程名称</label>
              <input type="text" id="editCourseName" name="courseName" value="${course.courseName}" required>
            </div>
            <div class="form-group">
              <label for="editDiscipline">学科</label>
              <input type="text" id="editDiscipline" name="discipline" value="${course.discipline}" required>
            </div>
            <div class="form-group">
              <label for="editDescription">课程描述</label>
              <textarea id="editDescription" name="description" rows="4">${course.description || ""}</textarea>
            </div>
            <div class="form-actions">
              <button type="button" class="btn btn-secondary" onclick="closeModal()">取消</button>
              <button type="submit" class="btn btn-primary">保存修改</button>
            </div>
          </form>
        `

        showModal("编辑课程", content)

        document.getElementById("editCourseForm").addEventListener("submit", async (e) => {
          e.preventDefault()
          const formData = new FormData(e.target)
          const courseName = formData.get("courseName")
          const discipline = formData.get("discipline")
          const description = formData.get("description")

          try {
            const updateResponse = await fetch(
                `${API_BASE_URL}/teacher/updateCourse?courseName=${encodeURIComponent(courseName)}&discipline=${encodeURIComponent(discipline)}&description=${encodeURIComponent(description)}`,
            )

            if (updateResponse.ok) {
              showNotification("课程更新成功", "success")
              closeModal()
              loadCourses()
            } else {
              showNotification("更新失败，请稍后重试", "error")
            }
          } catch (error) {
            console.error("更新课程失败:", error)
            showNotification("网络错误，请稍后重试", "error")
          }
        })
      } else {
        showNotification("课程不存在", "error")
      }
    } else {
      showNotification("获取课程信息失败", "error")
    }
  } catch (error) {
    console.error("编辑课程失败:", error)
    showNotification("网络错误，请稍后重试", "error")
  } finally {
    showLoading(false)
  }
}

// 查看教学设计
async function viewLesson(lessonPlanId) {
  try {
    showLoading(true)
    const response = await fetch(`${API_BASE_URL}/teacher/getAllLessonPlans`)
    if (response.ok) {
      const result = await response.json()
      const lessonPlans = result.data || result
      const lesson = lessonPlans.find((l) => l.lessonPlanId === lessonPlanId)

      if (lesson) {
        const content = `
          <div class="lesson-detail">
            <div class="detail-item">
              <label>教学设计ID:</label>
              <span>${lesson.lessonPlanId}</span>
            </div>
            <div class="detail-item">
              <label>标题:</label>
              <span>${lesson.title}</span>
            </div>
            <div class="detail-item">
              <label>教师ID:</label>
              <span>${lesson.teacherId}</span>
            </div>
            <div class="detail-item">
              <label>课程ID:</label>
              <span>${lesson.courseId}</span>
            </div>
            <div class="detail-item">
              <label>创建时间:</label>
              <span>${formatDate(lesson.createTime)}</span>
            </div>
            <div class="detail-content">
              <label>教学内容:</label>
              <div class="content-text">${lesson.content}</div>
            </div>
          </div>
          <style>
            .lesson-detail { padding: 20px; max-height: 60vh; overflow-y: auto; }
            .detail-item { display: flex; margin-bottom: 15px; }
            .detail-item label { min-width: 120px; font-weight: 600; color: #2c3e50; }
            .detail-item span { color: #6c757d; }
            .detail-content { margin-top: 20px; }
            .detail-content label { font-weight: 600; color: #2c3e50; display: block; margin-bottom: 10px; }
            .content-text { background: #f8f9fa; padding: 15px; border-radius: 8px; line-height: 1.6; white-space: pre-wrap; }
          </style>
        `
        showModal("教学设计详情", content)
      } else {
        showNotification("教学设计不存在", "error")
      }
    } else {
      showNotification("获取教学设计详情失败", "error")
    }
  } catch (error) {
    console.error("查看教学设计失败:", error)
    showNotification("网络错误，请稍后重试", "error")
  } finally {
    showLoading(false)
  }
}

// 编辑教学设计
async function editLesson(lessonPlanId) {
  try {
    showLoading(true)
    const response = await fetch(`${API_BASE_URL}/teacher/getAllLessonPlans`)
    if (response.ok) {
      const result = await response.json()
      const lessonPlans = result.data || result
      const lesson = lessonPlans.find((l) => l.lessonPlanId === lessonPlanId)

      if (lesson) {
        // 获取课程信息用于表单
        const coursesResponse = await fetch(`${API_BASE_URL}/teacher/getAllCourses`)
        const coursesResult = await coursesResponse.json()
        const courses = coursesResult.data || coursesResult
        const currentCourse = courses.find((c) => c.courseId === lesson.courseId)

        const content = `
          <form id="editLessonForm">
            <input type="hidden" name="lessonPlanId" value="${lesson.lessonPlanId}">
            <div class="form-group">
              <label for="editLessonCourseName">课程名称</label>
              <input type="text" id="editLessonCourseName" name="courseName" value="${currentCourse ? currentCourse.courseName : ""}" required>
            </div>
            <div class="form-group">
              <label for="editLessonTitle">标题</label>
              <input type="text" id="editLessonTitle" name="title" value="${lesson.title}" required>
            </div>
            <div class="form-group">
              <label for="editLessonContent">教学内容</label>
              <textarea id="editLessonContent" name="content" rows="10" required>${lesson.content}</textarea>
            </div>
            <div class="form-actions">
              <button type="button" class="btn btn-secondary" onclick="closeModal()">取消</button>
              <button type="submit" class="btn btn-primary">保存修改</button>
            </div>
          </form>
        `

        showModal("编辑教学设计", content)

        document.getElementById("editLessonForm").addEventListener("submit", async (e) => {
          e.preventDefault()
          const formData = new FormData(e.target)
          const lessonPlanId = formData.get("lessonPlanId")
          const courseName = formData.get("courseName")
          const title = formData.get("title")
          const content = formData.get("content")

          try {
            const updateResponse = await fetch(
                `${API_BASE_URL}/teacher/updateLessonPlan?lessonPlanId=${lessonPlanId}&courseName=${encodeURIComponent(courseName)}&title=${encodeURIComponent(title)}&content=${encodeURIComponent(content)}`,
            )

            if (updateResponse.ok) {
              showNotification("教学设计更新成功", "success")
              closeModal()
              loadLessonPlans()
            } else {
              showNotification("更新失败，请稍后重试", "error")
            }
          } catch (error) {
            console.error("更新教学设计失败:", error)
            showNotification("网络错误，请稍后重试", "error")
          }
        })
      } else {
        showNotification("教学设计不存在", "error")
      }
    } else {
      showNotification("获取教学设计信息失败", "error")
    }
  } catch (error) {
    console.error("编辑教学设计失败:", error)
    showNotification("网络错误，请稍后重试", "error")
  } finally {
    showLoading(false)
  }
}

// 查看题目
async function viewQuestion(questionId) {
  try {
    showLoading(true)
    const response = await fetch(`${API_BASE_URL}/teacher/getAllQuestions`)
    if (response.ok) {
      const result = await response.json()
      const questions = result.data || result
      const question = questions.find((q) => q.questionId === questionId)

      if (question) {
        const content = `
          <div class="question-detail">
            <div class="detail-item">
              <label>题目ID:</label>
              <span>${question.questionId}</span>
            </div>
            <div class="detail-item">
              <label>教学设计ID:</label>
              <span>${question.lessonPlanId}</span>
            </div>
            <div class="detail-item">
              <label>题目类型:</label>
              <span class="badge badge-${getQuestionTypeBadgeClass(question.questionType)}">${getQuestionTypeDisplayName(question.questionType)}</span>
            </div>
            <div class="detail-item">
              <label>知识点:</label>
              <span>${question.knowledgePoint}</span>
            </div>
            <div class="detail-item">
              <label>创建时间:</label>
              <span>${formatDate(question.createTime)}</span>
            </div>
            <div class="detail-content">
              <label>题目内容:</label>
              <div class="content-text">${question.questionText}</div>
            </div>
            <div class="detail-content">
              <label>参考答案:</label>
              <div class="content-text">${question.referenceAnswer}</div>
            </div>
          </div>
          <style>
            .question-detail { padding: 20px; max-height: 60vh; overflow-y: auto; }
            .detail-item { display: flex; margin-bottom: 15px; align-items: center; }
            .detail-item label { min-width: 120px; font-weight: 600; color: #2c3e50; }
            .detail-item span { color: #6c757d; }
            .detail-content { margin-top: 20px; }
            .detail-content label { font-weight: 600; color: #2c3e50; display: block; margin-bottom: 10px; }
            .content-text { background: #f8f9fa; padding: 15px; border-radius: 8px; line-height: 1.6; white-space: pre-wrap; }
          </style>
        `
        showModal("题目详情", content)
      } else {
        showNotification("题目不存在", "error")
      }
    } else {
      showNotification("获取题目详情失败", "error")
    }
  } catch (error) {
    console.error("查看题目失败:", error)
    showNotification("网络错误，请稍后重试", "error")
  } finally {
    showLoading(false)
  }
}

// 编辑题目
async function editQuestion(questionId) {
  try {
    showLoading(true)
    const response = await fetch(`${API_BASE_URL}/teacher/getAllQuestions`)
    if (response.ok) {
      const result = await response.json()
      const questions = result.data || result
      const question = questions.find((q) => q.questionId === questionId)

      if (question) {
        const content = `
          <form id="editQuestionForm">
            <input type="hidden" name="questionId" value="${question.questionId}">
            <div class="form-group">
              <label for="editQuestionType">题目类型</label>
              <select id="editQuestionType" name="questionType" required>
                <option value="multiple_choice" ${question.questionType === "multiple_choice" ? "selected" : ""}>选择题</option>
                <option value="programming" ${question.questionType === "programming" ? "selected" : ""}>编程题</option>
                <option value="short_answer" ${question.questionType === "short_answer" ? "selected" : ""}>简答题</option>
              </select>
            </div>
            <div class="form-group">
              <label for="editKnowledgePoint">知识点</label>
              <input type="text" id="editKnowledgePoint" name="knowledgePoint" value="${question.knowledgePoint}" required>
            </div>
            <div class="form-group">
              <label for="editQuestionText">题目内容</label>
              <textarea id="editQuestionText" name="questionText" rows="5" required>${question.questionText}</textarea>
            </div>
            <div class="form-group">
              <label for="editReferenceAnswer">参考答案</label>
              <textarea id="editReferenceAnswer" name="referenceAnswer" rows="5" required>${question.referenceAnswer}</textarea>
            </div>
            <div class="form-actions">
              <button type="button" class="btn btn-secondary" onclick="closeModal()">取消</button>
              <button type="submit" class="btn btn-primary">保存修改</button>
            </div>
          </form>
        `

        showModal("编辑题目", content)

        document.getElementById("editQuestionForm").addEventListener("submit", async (e) => {
          e.preventDefault()
          const formData = new FormData(e.target)
          const questionId = formData.get("questionId")
          const questionText = formData.get("questionText")
          const referenceAnswer = formData.get("referenceAnswer")
          const questionType = formData.get("questionType")
          const knowledgePoint = formData.get("knowledgePoint")

          try {
            const updateResponse = await fetch(
                `${API_BASE_URL}/teacher/updateQuestion?questionId=${questionId}&question=${encodeURIComponent(questionText)}&answer=${encodeURIComponent(referenceAnswer)}&questionType=${encodeURIComponent(questionType)}&knowledgePoint=${encodeURIComponent(knowledgePoint)}`,
            )

            if (updateResponse.ok) {
              showNotification("题目更新成功", "success")
              closeModal()
              loadQuestions()
            } else {
              showNotification("更新失败，请稍后重试", "error")
            }
          } catch (error) {
            console.error("更新题目失败:", error)
            showNotification("网络错误，请稍后重试", "error")
          }
        })
      } else {
        showNotification("题目不存在", "error")
      }
    } else {
      showNotification("获取题目信息失败", "error")
    }
  } catch (error) {
    console.error("编辑题目失败:", error)
    showNotification("网络错误，请稍后重试", "error")
  } finally {
    showLoading(false)
  }
}

// 编辑用户
async function editUser(username) {
  try {
    showLoading(true)
    const response = await fetch(`${API_BASE_URL}/admin/viewUsers`)
    if (response.ok) {
      const result = await response.json()
      const users = result.data || result
      const user = users.find((u) => u.username === username)

      if (user) {
        const content = `
          <form id="editUserForm">
            <input type="hidden" name="userId" value="${user.userId}">
            <div class="form-group">
              <label for="editUserUsername">用户名</label>
              <input type="text" id="editUserUsername" name="username" value="${user.username}" required readonly>
              <small class="form-text text-muted">用户名不可修改</small>
            </div>
            <div class="form-group">
              <label for="editUserPassword">新密码</label>
              <input type="password" id="editUserPassword" name="password" placeholder="留空则不修改密码">
            </div>
            <div class="form-group">
              <label for="editUserRole">角色</label>
              <select id="editUserRole" name="role" required>
                <option value="admin" ${user.role === "admin" ? "selected" : ""}>管理员</option>
                <option value="teacher" ${user.role === "teacher" ? "selected" : ""}>教师</option>
                <option value="student" ${user.role === "student" ? "selected" : ""}>学生</option>
              </select>
            </div>
            <div class="form-group">
              <label for="editUserEmail">邮箱</label>
              <input type="email" id="editUserEmail" name="email" value="${user.email || ""}">
            </div>
            <div class="form-group">
              <label for="editUserRealName">真实姓名</label>
              <input type="text" id="editUserRealName" name="realName" value="${user.realName || ""}">
            </div>
            <div class="form-group">
              <label for="editUserCode">编号</label>
              <input type="text" id="editUserCode" name="code" value="${user.code || ""}">
            </div>
            <div class="form-actions">
              <button type="button" class="btn btn-secondary" onclick="closeModal()">取消</button>
              <button type="submit" class="btn btn-primary">保存修改</button>
            </div>
          </form>
          <style>
            .form-text { font-size: 0.875rem; color: #6c757d; margin-top: 5px; }
            .text-muted { color: #6c757d !important; }
          </style>
        `

        showModal("编辑用户", content)

        document.getElementById("editUserForm").addEventListener("submit", async (e) => {
          e.preventDefault()
          const formData = new FormData(e.target)

          const params = new URLSearchParams()
          for (const [key, value] of formData.entries()) {
            if (key !== "userId" && value) {
              params.append(key, value)
            }
          }

          try {
            const updateResponse = await fetch(`${API_BASE_URL}/admin/updateUser?${params.toString()}`)

            if (updateResponse.ok) {
              showNotification("用户信息更新成功", "success")
              closeModal()
              loadUsers()
            } else {
              showNotification("更新失败，请稍后重试", "error")
            }
          } catch (error) {
            console.error("更新用户失败:", error)
            showNotification("网络错误，请稍后重试", "error")
          }
        })
      } else {
        showNotification("用户不存在", "error")
      }
    } else {
      showNotification("获取用户信息失败", "error")
    }
  } catch (error) {
    console.error("编辑用户失败:", error)
    showNotification("网络错误，请稍后重试", "error")
  } finally {
    showLoading(false)
  }
}

// 导出所有资源
async function exportAllResources() {
  try {
    showLoading(true)

    // 获取所有教学设计
    const response = await fetch(`${API_BASE_URL}/teacher/getAllLessonPlans`)
    if (response.ok) {
      const result = await response.json()
      const lessonPlans = result.data || result

      if (lessonPlans.length === 0) {
        showNotification("暂无可导出的资源", "warning")
        return
      }

      // 显示导出选择对话框
      const lessonPlanOptions = lessonPlans
          .map(
              (lesson) =>
                  `<label class="checkbox-item">
          <input type="checkbox" name="lessonPlan" value="${lesson.lessonPlanId}" checked>
          <span>${lesson.title}</span>
        </label>`,
          )
          .join("")

      const content = `
        <form id="exportResourcesForm">
          <div class="export-options">
            <h4>选择要导出的教学设计:</h4>
            <div class="checkbox-group">
              <label class="checkbox-item select-all">
                <input type="checkbox" id="selectAll" checked>
                <span><strong>全选</strong></span>
              </label>
              ${lessonPlanOptions}
            </div>
          </div>
          <div class="form-actions">
            <button type="button" class="btn btn-secondary" onclick="closeModal()">取消</button>
            <button type="submit" class="btn btn-primary">
              <i class="fas fa-download"></i> 导出选中资源
            </button>
          </div>
        </form>
        <style>
          .export-options { padding: 20px; }
          .checkbox-group { max-height: 300px; overflow-y: auto; border: 1px solid #dee2e6; border-radius: 8px; padding: 15px; margin-top: 15px; }
          .checkbox-item { display: flex; align-items: center; margin-bottom: 10px; padding: 8px; border-radius: 4px; transition: background 0.2s; }
          .checkbox-item:hover { background: #f8f9fa; }
          .checkbox-item input { margin-right: 10px; }
          .select-all { border-bottom: 1px solid #dee2e6; margin-bottom: 15px; padding-bottom: 15px; }
        </style>
      `

      showModal("导出资源", content)

      // 全选功能
      document.getElementById("selectAll").addEventListener("change", (e) => {
        const checkboxes = document.querySelectorAll('input[name="lessonPlan"]')
        checkboxes.forEach((checkbox) => {
          checkbox.checked = e.target.checked
        })
      })

      // 提交导出
      document.getElementById("exportResourcesForm").addEventListener("submit", async (e) => {
        e.preventDefault()

        const selectedLessonPlans = Array.from(document.querySelectorAll('input[name="lessonPlan"]:checked')).map(
            (input) => input.value,
        )

        if (selectedLessonPlans.length === 0) {
          showNotification("请至少选择一个资源", "warning")
          return
        }

        showLoading(true)
        closeModal()

        try {
          // 批量导出
          const exportPromises = selectedLessonPlans.map(async (lessonPlanId) => {
            const response = await fetch(`${API_BASE_URL}/admin/exportResource?lessonPlanId=${lessonPlanId}`)
            if (response.ok) {
              const blob = await response.blob()
              const lesson = lessonPlans.find((l) => l.lessonPlanId == lessonPlanId)
              return {
                blob,
                filename: `${lesson.title}_${lessonPlanId}.pdf`,
              }
            }
            return null
          })

          const results = await Promise.all(exportPromises)
          const successResults = results.filter((r) => r !== null)

          // 下载所有文件
          successResults.forEach((result) => {
            const url = window.URL.createObjectURL(result.blob)
            const a = document.createElement("a")
            a.href = url
            a.download = result.filename
            document.body.appendChild(a)
            a.click()
            window.URL.revokeObjectURL(url)
            document.body.removeChild(a)
          })

          if (successResults.length > 0) {
            showNotification(`成功导出 ${successResults.length} 个资源`, "success")
          } else {
            showNotification("导出失败，请稍后重试", "error")
          }
        } catch (error) {
          console.error("批量导出失败:", error)
          showNotification("导出过程中发生错误", "error")
        } finally {
          showLoading(false)
        }
      })
    } else {
      showNotification("获取资源列表失败", "error")
    }
  } catch (error) {
    console.error("导出资源失败:", error)
    showNotification("网络错误，请稍后重试", "error")
  } finally {
    showLoading(false)
  }
}

// 加载资料列表
async function loadMaterials() {
  try {
    const response = await fetch(`${API_BASE_URL}/teacher/getAllMaterial`)
    if (response.ok) {
      const result = await response.json()
      const materials = result.data || result
      renderMaterialsGrid(materials)
    }
  } catch (error) {
    console.error("加载资料列表失败:", error)
  }
}

// 渲染资料网格
function renderMaterialsGrid(materials) {
  // 检查是否有资料管理区域，如果没有则创建
  const materialsSection = document.getElementById("teacher-materials-list")
  if (!materialsSection) {
    // 在资料上传区域下方添加资料列表
    const materialsContainer = document.getElementById("teacher-materials")
    if (materialsContainer) {
      const listSection = document.createElement("div")
      listSection.innerHTML = `
        <h3 style="margin-top: 30px; margin-bottom: 20px;">已上传资料</h3>
        <div id="materialsGrid" class="materials-grid"></div>
      `
      materialsContainer.appendChild(listSection)
    }
  }

  const grid = document.getElementById("materialsGrid")
  if (!grid) return

  grid.innerHTML = ""

  if (materials.length === 0) {
    grid.innerHTML = `
      <div class="empty-state">
        <i class="fas fa-file"></i>
        <h3>暂无资料</h3>
        <p>上传文件后会显示在这里</p>
      </div>
    `
    return
  }

  materials.forEach((material) => {
    const card = document.createElement("div")
    card.className = "material-card card-hover"
    card.innerHTML = `
      <div class="material-info">
        <i class="fas fa-file-alt"></i>
        <div>
          <h4>${material.title}</h4>
          <p><span class="badge badge-info">${material.materialType}</span></p>
          <p class="text-muted">${formatDate(material.createTime)}</p>
        </div>
      </div>
      <div class="card-actions">
        <button class="btn btn-danger btn-sm" onclick="deleteMaterial(${material.materialId})">
          <i class="fas fa-trash"></i> 删除
        </button>
      </div>
    `
    grid.appendChild(card)
  })
}

// 删除课程
async function deleteCourse(courseName) {
  if (!confirm(`确定要删除课程"${courseName}"吗？此操作不可撤销！`)) {
    return
  }

  try {
    showLoading(true)
    const response = await fetch(`${API_BASE_URL}/teacher/deleteCourse?courseName=${encodeURIComponent(courseName)}`)

    if (response.ok) {
      showNotification("课程删除成功", "success")
      loadCourses()
    } else {
      showNotification("删除失败，请稍后重试", "error")
    }
  } catch (error) {
    console.error("删除课程失败:", error)
    showNotification("网络错误，请稍后重试", "error")
  } finally {
    showLoading(false)
  }
}

// 删除教学设计
async function deleteLessonPlan(lessonPlanId) {
  if (!confirm("确定要删除这个教学设计吗？此操作不可撤销！")) {
    return
  }

  try {
    showLoading(true)
    const response = await fetch(`${API_BASE_URL}/teacher/deleteLessonPlan?lessonPlanId=${lessonPlanId}`)

    if (response.ok) {
      showNotification("教学设计删除成功", "success")
      loadLessonPlans()
    } else {
      showNotification("删除失败，请稍后重试", "error")
    }
  } catch (error) {
    console.error("删除教学设计失败:", error)
    showNotification("网络错误，请稍后重试", "error")
  } finally {
    showLoading(false)
  }
}

// 删除题目
async function deleteQuestion(questionId) {
  if (!confirm("确定要删除这个题目吗？此操作不可撤销！")) {
    return
  }

  try {
    showLoading(true)
    const response = await fetch(`${API_BASE_URL}/teacher/deleteQuestion?questionId=${questionId}`)

    if (response.ok) {
      showNotification("题目删除成功", "success")
      loadQuestions()
    } else {
      showNotification("删除失败，请稍后重试", "error")
    }
  } catch (error) {
    console.error("删除题目失败:", error)
    showNotification("网络错误，请稍后重试", "error")
  } finally {
    showLoading(false)
  }
}

// 删除资料
async function deleteMaterial(materialId) {
  if (!confirm("确定要删除这个资料吗？此操作不可撤销！")) {
    return
  }

  try {
    showLoading(true)
    const response = await fetch(`${API_BASE_URL}/teacher/deleteMaterial?materialId=${materialId}`)

    if (response.ok) {
      showNotification("资料删除成功", "success")
      loadMaterials()
    } else {
      showNotification("删除失败，请稍后重试", "error")
    }
  } catch (error) {
    console.error("删除资料失败:", error)
    showNotification("网络错误，请稍后重试", "error")
  } finally {
    showLoading(false)
  }
}

function escapeHtml(text) {
  const map = {
    "&": "&amp;",
    "<": "&lt;",
    ">": "&gt;",
    '"': "&quot;",
    "'": "&#039;",
  }
  return text.replace(/[&<>"']/g, (m) => map[m])
}

function truncateMarkdown(markdown, length = 200) {
  const html = marked.parse(markdown || "")
  const div = document.createElement("div")
  div.innerHTML = html
  const text = div.textContent || div.innerText || ""
  return text.length > length ? text.substring(0, length) + "..." : text
}
