<template>
    <div class="page-card">
        <div class="page-toolbar">
            <div class="toolbar-right">
                <el-input v-model="searchKeyword" placeholder="搜索账号 / 角色" class="search-field" />
                <el-button type="primary" @click="openDialog">
                    <el-icon class="btn-icon"><Plus /></el-icon>新增账户
                </el-button>
            </div>
        </div>

        <el-table :data="paginatedList" border stripe class="data-table" :header-cell-style="{background:'#f8fafc',color:'#64748b',fontWeight:'500'}" row-key="id">
            <el-table-column type="index" label="序号" width="70" align="center" />
            <el-table-column prop="username" label="账号" min-width="120" align="center" />
            <el-table-column label="密码" width="160" align="center">
                <template #default="scope">
                    <div class="password-cell">
                        <span class="password-text">{{ scope.row.passwordVisible ? scope.row.plainPassword : '••••••••' }}</span>
                        <el-icon class="password-eye" @click="togglePassword(scope.row)">
                            <component :is="scope.row.passwordVisible ? 'View' : 'Hide'" />
                        </el-icon>
                    </div>
                </template>
            </el-table-column>
            <el-table-column prop="role" label="角色" min-width="140" align="center" />
            <el-table-column prop="phone" label="手机号" min-width="140" align="center" />
            <el-table-column prop="lastLogin" label="最近登录" min-width="180" align="center" />
            <el-table-column prop="status" label="状态" width="120" align="center">
                <template #default="scope">
                    <el-tag :type="scope.row.status==='正常'?'success':'danger'" size="small" round>{{scope.row.status}}</el-tag>
                </template>
            </el-table-column>
            <el-table-column label="操作" width="220" align="center">
                <template #default="scope">
                    <template v-if="scope.row.role !== '系统管理员'">
                        <el-button v-if="scope.row.status==='正常'" type="danger" size="small" @click="changeStatus(scope.row,'禁用')">
                            <el-icon class="btn-icon"><CircleClose /></el-icon>禁用
                        </el-button>
                        <el-button v-else type="success" size="small" @click="changeStatus(scope.row,'正常')">
                            <el-icon class="btn-icon"><CircleCheck /></el-icon>启用
                        </el-button>
                        <el-popconfirm title="确定要删除该用户吗？" @confirm="deleteUser(scope.row)">
                            <template #reference>
                                <el-button type="danger" size="small" link class="ml-8">
                                    <el-icon class="btn-icon"><Delete /></el-icon>删除
                                </el-button>
                            </template>
                        </el-popconfirm>
                    </template>
                    <span v-else class="text-muted">--</span>
                </template>
            </el-table-column>
        </el-table>

        <div class="pagination-bar">
            <el-pagination v-model:current-page="currentPage" v-model:page-size="pageSize" :page-sizes="[5,10,20,50]" layout="total,sizes,prev,pager,next" :total="total" @current-change="fetchUsers" />
        </div>

        <el-dialog v-model="dialogVisible" title="新增系统账户" width="420">
            <el-form ref="formRef" :model="formUser" :rules="rules" label-width="80px" style="margin:20px 10px">
                <el-form-item label="账号" prop="username"><el-input v-model="formUser.username" placeholder="请输入账号" clearable /></el-form-item>
                <el-form-item label="角色" prop="role">
                    <el-select v-model="formUser.role" placeholder="请选择角色" style="width:100%">
                        <el-option label="系统管理员" value="系统管理员" />
                        <el-option label="人事主管" value="人事主管" />
                        <el-option label="部门主管" value="部门主管" />
                    </el-select>
                </el-form-item>
                <el-form-item label="手机号" prop="phone"><el-input v-model="formUser.phone" placeholder="请输入手机号" clearable /></el-form-item>
            </el-form>
            <template #footer>
                <el-button @click="dialogVisible=false">取消</el-button>
                <el-button type="primary" @click="submit">确定</el-button>
            </template>
        </el-dialog>
    </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import api from '../api/index.js'

const togglePassword = (row) => {
  row.passwordVisible = !row.passwordVisible
}

const dialogVisible = ref(false)
const searchKeyword = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const userList = ref([])
const total = ref(0)

const fetchUsers = async () => {
  try {
    const res = await api.get('/users', { params: { page: currentPage.value, pageSize: pageSize.value, keyword: searchKeyword.value || undefined } })
    if (res.code === 200) { userList.value = res.data.records || []; total.value = res.data.total || 0 }
  } catch {}
}

const paginatedList = computed(() => userList.value)
const displayList = computed(() => userList.value)

const searchUser = () => { currentPage.value = 1; fetchUsers() }

let searchTimer
watch(searchKeyword, () => {
  clearTimeout(searchTimer)
  searchTimer = setTimeout(() => { searchUser() }, 300)
})

const formRef = ref()
const formUser = reactive({username:'',role:'',phone:''})
const rules = {
    username:[{required:true,message:'请输入账号名称',trigger:'blur'},{min:1,max:20,message:'长度在1到20个字符',trigger:'blur'}],
    role:[{required:true,message:'请选择角色',trigger:'change'}],
    phone:[{required:true,message:'请输入手机号',trigger:'blur'},{pattern:/^1\d{10}$/,message:'请输入合法手机号',trigger:'blur'}],
}

const openDialog = () => { formUser.username=''; formUser.role=''; formUser.phone=''; dialogVisible.value=true }
const submit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  try {
    await api.post('/users', { username: formUser.username, role: formUser.role, phone: formUser.phone })
    dialogVisible.value = false
    ElMessage.success('添加成功')
    fetchUsers()
  } catch { ElMessage.error('添加失败') }
}

const changeStatus = async (item, status) => {
  try { await api.put(`/users/${item.id}/status`, null, { params: { status } }); ElMessage.success('修改成功'); fetchUsers() } catch {}
}
const deleteUser = async (row) => {
  try { await api.delete(`/users/${row.id}`); ElMessage.success('删除成功'); fetchUsers() } catch {}
}
onMounted(() => { fetchUsers() })
</script>

<style scoped>
.page-card{background:#fff;border-radius:16px;padding:24px;box-shadow:0 1px 6px rgba(0,0,0,.04);border:1px solid #f1f3f6}
.page-toolbar{display:flex;justify-content:space-between;align-items:center}
.toolbar-left{display:flex;flex-direction:column}
.toolbar-title{font-size:18px;font-weight:600;color:#333}
.toolbar-desc{font-size:13px;color:#999;margin-top:2px}
.toolbar-right{display:flex;align-items:center;gap:10px}
.search-field{width:220px}
.btn-icon{margin-right:4px}
.ml-8{margin-left:8px}
.data-table{margin-top:16px}
.pagination-bar{display:flex;justify-content:flex-end;margin-top:24px}
.password-cell{display:flex;align-items:center;justify-content:center;gap:8px}
.password-text{font-family:monospace;letter-spacing:2px}
.password-eye{cursor:pointer;color:#909399;transition:color .2s}
.password-eye:hover{color:#409eff}
</style>
