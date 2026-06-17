<template>
  <div class="layout">
    <!-- 侧边栏 -->
    <aside class="side" :class="{ fold: folded }">
      <div class="logo" @click="$router.push('/admin')">
        <span class="logo-icon">🎫</span>
        <span v-if="!folded" class="lt">卡券商城</span>
      </div>
      <nav class="nav">
        <div v-for="m in menu" :key="m.k" class="mi" :class="{ on: cur === m.k }" @click="cur = m.k">
          <span class="mic">{{ m.icon }}</span>
          <span v-if="!folded" class="mil">{{ m.label }}</span>
          <span v-if="!folded && cur === m.k" class="mi-dot"></span>
        </div>
      </nav>
      <div class="sbot">
        <div v-if="!folded" class="urow">
          <img :src="avt" class="uavt" />
          <div>
            <div class="un">{{ aname }}</div>
            <div class="ur">超级管理员</div>
          </div>
        </div>
        <button class="lbtn" @click="logout">
          <span>🚪</span><span v-if="!folded">退出登录</span>
        </button>
      </div>
    </aside>

    <!-- 主区域 -->
    <main class="main">
      <div class="bar">
        <button class="fbtn" @click="folded = !folded">{{ folded ? '☰' : '✕' }}</button>
        <el-breadcrumb separator="/">
          <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
          <el-breadcrumb-item>{{ curName }}</el-breadcrumb-item>
        </el-breadcrumb>
        <div class="br">
          <span class="bell">🔔<i class="bdot"></i></span>
          <span class="abadge">{{ aname }}</span>
        </div>
      </div>

      <div class="content">
        <!-- ====== 数据中心 ====== -->
        <div v-show="cur === 'dashboard'">
          <!-- 统计卡片 -->
          <div class="sg">
            <div v-for="s in stats" :key="s.label" class="sc" :class="s.cls">
              <div class="sc-icon"><span>{{ s.icon }}</span></div>
              <div class="sc-info">
                <span class="sv">{{ s.value }}</span>
                <span class="sl">{{ s.label }}</span>
              </div>
              <div class="sc-trend" v-if="s.trend">↑</div>
            </div>
          </div>

          <!-- 快捷操作 -->
          <div class="qa">
            <div class="qa-head">
              <span class="ql">⚡ 快捷操作</span>
              <span class="qa-hint">常用功能入口</span>
            </div>
            <div class="qb">
              <button v-for="a in qacts" :key="a" class="qbtn">{{ a }}</button>
            </div>
          </div>

          <!-- 图表 + 排行 -->
          <div class="twocol">
            <div class="pn">
              <div class="ph">
                <div>
                  <h3>📊 近30天趋势</h3>
                  <p class="ph-sub">订单 · 交易 · 注册</p>
                </div>
                <div class="lg">
                  <span class="lg-item"><i style="background:#f43f5e"></i>订单</span>
                  <span class="lg-item"><i style="background:#3b82f6"></i>交易</span>
                  <span class="lg-item"><i style="background:#10b981"></i>注册</span>
                </div>
              </div>
              <div ref="chart" class="ch"></div>
            </div>
            <div class="pn">
              <div class="ph">
                <div>
                  <h3>🔥 热销排行</h3>
                  <p class="ph-sub">本月销量TOP3</p>
                </div>
              </div>
              <div class="hl">
                <div v-for="(h,i) in hot" :key="i" class="hi">
                  <span class="rk" :class="'r'+(i+1)">{{ i+1 }}</span>
                  <div class="hi-info">
                    <span class="hn">{{ h.name }}</span>
                    <div class="hi-bar"><div class="hi-fill" :style="{ width: (100 - i*30) + '%' }"></div></div>
                  </div>
                  <span class="ho">{{ h.orders }}单</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- ====== 商品管理 ====== -->
        <div v-show="cur === 'goods'" class="pn">
          <div class="ph">
            <div>
              <h3>📦 商品管理</h3>
              <p class="ph-sub">共 {{ glist.length }} 件商品</p>
            </div>
            <el-button type="primary" @click="openG()">+ 新增商品</el-button>
          </div>
          <el-table :data="glist" border stripe style="border-radius:12px;overflow:hidden">
            <el-table-column prop="id" label="ID" width="60" />
            <el-table-column prop="goodsName" label="商品名称" min-width="140" />
            <el-table-column prop="price" label="价格" width="90">
              <template #default="{ row }"><span style="color:#f43f5e;font-weight:700">¥{{ row.price }}</span></template>
            </el-table-column>
            <el-table-column prop="stock" label="库存" width="70" />
            <el-table-column prop="status" label="状态" width="80">
              <template #default="{ row }">
                <el-tag :type="row.status===1?'success':'danger'" size="small" effect="plain">{{ row.status===1?'上架':'下架' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="150" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" @click="openG(row)">编辑</el-button>
                <el-button link type="danger" @click="delG(row.id)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>

        <!-- ====== 用户管理 ====== -->
        <div v-show="cur === 'user'" class="pn">
          <div class="ph">
            <div>
              <h3>👥 用户管理</h3>
              <p class="ph-sub">共 <b>{{ ulist.length }}</b> 位注册用户</p>
            </div>
          </div>
          <el-table :data="ulist" border stripe style="border-radius:12px;overflow:hidden">
            <el-table-column prop="id" label="ID" width="60" />
            <el-table-column prop="username" label="账号" min-width="100" />
            <el-table-column prop="nickName" label="昵称" min-width="100" />
            <el-table-column prop="phone" label="手机号" min-width="110" />
            <el-table-column prop="email" label="邮箱" min-width="130" />
            <el-table-column prop="createTime" label="注册时间" :formatter="fmt" min-width="140" />
            <el-table-column label="操作" width="150" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" @click="openU(row)">修改</el-button>
                <el-button link type="danger" @click="delU(row.id)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>

        <!-- ====== 订单列表 ====== -->
        <div v-show="cur === 'order'" class="pn">
          <div class="ph">
            <div>
              <h3>📋 全平台订单</h3>
              <p class="ph-sub">共 {{ orderTotal }} 笔订单</p>
            </div>
          </div>
          <el-table :data="olist" border stripe style="border-radius:12px;overflow:hidden">
            <el-table-column prop="id" label="订单号" width="75" />
            <el-table-column label="用户" min-width="90">
              <template #default="{ row }">{{ getUserName(row.userId) }}</template>
            </el-table-column>
            <el-table-column label="商品" min-width="130">
              <template #default="{ row }">{{ getGoodsName(row.goodsId) }}</template>
            </el-table-column>
            <el-table-column prop="orderPrice" label="金额" width="90">
              <template #default="{ row }"><span style="color:#f43f5e;font-weight:700">¥{{ row.orderPrice }}</span></template>
            </el-table-column>
            <el-table-column label="支付方式" width="90">
              <template #default="{ row }">{{ payLabel2(row.payMethod) }}</template>
            </el-table-column>
            <el-table-column prop="accountNumber" label="充值账号" min-width="110" />
            <el-table-column label="状态" width="80">
              <template #default="{ row }">
                <el-tag :type="row.status==='completed'?'success':row.status==='refunded'?'info':'warning'" size="small">{{ stxt2(row.status) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="下单时间" :formatter="fmt" width="150" />
          </el-table>
          <div style="margin-top:14px;display:flex;justify-content:center">
            <el-pagination
              :current-page="orderPage"
              :page-size="orderPageSize"
              :total="orderTotal"
              :page-sizes="[10, 20, 50]"
              layout="total, sizes, prev, pager, next"
              @update:current-page="(v) => { orderPage = v; loadO() }"
              @update:page-size="(v) => { orderPageSize = v; orderPage = 1; loadO() }"
            />
          </div>
        </div>

        <!-- ====== 通知管理 ====== -->
        <div v-show="cur === 'notify'" class="pn">
          <div class="ph">
            <div>
              <h3>🔔 站内通知</h3>
              <p class="ph-sub">共 {{ nlist.length }} 条通知</p>
            </div>
            <el-button type="primary" @click="openN()">+ 新增通知</el-button>
          </div>
          <el-table :data="nlist" border stripe style="border-radius:12px;overflow:hidden">
            <el-table-column prop="id" label="ID" width="60" />
            <el-table-column prop="title" label="通知标题" min-width="200" />
            <el-table-column prop="content" label="内容" min-width="140" show-overflow-tooltip />
            <el-table-column prop="status" label="状态" width="80">
              <template #default="{ row }">
                <el-tag :type="row.status===1?'success':'info'" size="small">{{ row.status===1?'显示中':'已隐藏' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="创建时间" :formatter="fmt" width="160" />
            <el-table-column label="操作" width="180" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" @click="openN(row)">编辑</el-button>
                <el-button link :type="row.status===1?'warning':'success'" @click="toggleN(row)">
                  {{ row.status===1?'隐藏':'显示' }}
                </el-button>
                <el-button link type="danger" @click="delN(row.id)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>

        <!-- ====== 优惠券管理 ====== -->
        <div v-show="cur === 'coupon'" class="pn">
          <div class="ph">
            <div>
              <h3>🎫 优惠卡券</h3>
              <p class="ph-sub">共 {{ couponList.length }} 张优惠券</p>
            </div>
            <el-button type="primary" @click="openCp()">+ 新增优惠券</el-button>
          </div>
          <el-table :data="couponList" border stripe style="border-radius:12px;overflow:hidden">
            <el-table-column prop="id" label="ID" width="60" />
            <el-table-column prop="title" label="券名称" min-width="140" />
            <el-table-column prop="amount" label="优惠金额" width="100">
              <template #default="{ row }"><span style="color:#f43f5e;font-weight:700">¥{{ row.amount }}</span></template>
            </el-table-column>
            <el-table-column prop="minAmount" label="最低消费" width="100">
              <template #default="{ row }">¥{{ row.minAmount || 0 }}</template>
            </el-table-column>
            <el-table-column prop="stock" label="库存" width="70" />
            <el-table-column prop="status" label="状态" width="80">
              <template #default="{ row }">
                <el-tag :type="row.status===1?'success':'info'" size="small">{{ row.status===1?'上架':'下架' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="180" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" @click="openCp(row)">编辑</el-button>
                <el-button link type="danger" @click="delCp(row.id)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>

        <!-- ====== 分类管理 ====== -->
        <div v-show="cur === 'category'" class="pn">
          <div class="ph">
            <div><h3>📂 分类管理</h3><p class="ph-sub">一级分类 → 二级分类（文件夹）→ 商品</p></div>
            <el-button type="primary" @click="openCat()">+ 新增一级</el-button>
          </div>
          <!-- 树形分类列表 -->
          <div class="cat-tree">
            <div v-for="l1 in catTree" :key="l1.id" class="ct-node">
              <!-- 一级分类 -->
              <div class="ct-row ct-l1" @click="toggleCat(l1.id)">
                <span class="ct-arrow">{{ catOpen[l1.id] ? '▼' : '▶' }}</span>
                <span class="ct-icon">📁</span>
                <span class="ct-name">{{ l1.cateName }}</span>
                <span class="ct-tag">一级</span>
                <span class="ct-info" v-if="l1.children">({{ l1.children.length }}个子分类)</span>
                <div class="ct-actions" @click.stop>
                  <el-button link type="primary" size="small" @click="openCat(l1)">编辑</el-button>
                  <el-button link type="primary" size="small" @click="addChild(l1.id)">+添加子分类</el-button>
                  <el-button link type="danger" size="small" @click="delCat(l1.id)">删除</el-button>
                </div>
              </div>
              <!-- 二级分类（子分类） -->
              <div v-if="catOpen[l1.id] && l1.children" class="ct-children">
                <div v-for="l2 in l1.children" :key="l2.id">
                  <div class="ct-row ct-l2" @click="toggleCatGoods(l2.id)">
                    <span class="ct-gap"></span>
                    <span class="ct-arrow">{{ catGoodsOpen[l2.id] ? '▼' : '▶' }}</span>
                    <img v-if="l2.icon || getCatFirstImg(l2.id)" :src="l2.icon || getCatFirstImg(l2.id)" style="width:22px;height:22px;border-radius:4px;object-fit:cover;flex-shrink:0" @error="$event.target.style.display='none'" />
                    <span v-if="!l2.icon && !getCatFirstImg(l2.id)" class="ct-icon">📄</span>
                    <span class="ct-name">{{ l2.cateName }}</span>
                    <span class="ct-tag ct-tag-l2">二级</span>
                    <span class="ct-info">{{ getCatGoodsCount(l2.id) }}个商品</span>
                    <div class="ct-actions" @click.stop>
                      <el-button link type="success" size="small" @click="addGoodsToCate(l2.id)">+商品</el-button>
                      <el-button link type="primary" size="small" @click="openCat(l2)">编辑</el-button>
                      <el-button link type="danger" size="small" @click="delCat(l2.id)">删除</el-button>
                    </div>
                  </div>
                  <!-- 商品列表 -->
                  <div v-if="catGoodsOpen[l2.id]" class="ct-goods">
                    <div v-if="getCatGoods(l2.id).length === 0" class="ct-empty">暂无商品</div>
                    <div v-for="g in getCatGoods(l2.id)" :key="g.id" class="ct-row ct-goods-row">
                      <span class="ct-gap" style="width:55px"></span>
                      <span style="color:#94a3b8;font-weight:700;flex-shrink:0">-</span>
                      <img :src="getGoodsThumb(g)" style="width:24px;height:24px;border-radius:4px;object-fit:cover;flex-shrink:0" @error="$event.target.style.display='none'" />
                      <span class="ct-name">{{ g.id }} {{ g.goodsName }}</span>
                      <span style="color:#f43f5e;font-weight:700;font-size:13px">¥{{ g.price }}</span>
                      <div class="ct-actions">
                        <el-button link type="primary" size="small" @click="openG(g)">编辑</el-button>
                      </div>
                    </div>
                  </div>
                </div>
                <div v-if="!l1.children.length" class="ct-empty">暂无子分类，点击「+添加子分类」</div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </main>

    <!-- 分类弹窗 -->
    <el-dialog v-model="catDia" :title="catf.id?'编辑分类':'新增分类'" width="440px" :close-on-click-modal="false">
      <el-form :model="catf" label-width="80px">
        <el-form-item label="分类名称"><el-input v-model="catf.cateName" placeholder="如：视频会员" /></el-form-item>
        <el-form-item label="图标URL"><el-input v-model="catf.icon" placeholder="图片地址，选填" /></el-form-item>
        <el-form-item label="父级分类">
          <el-select v-model="catf.parentId" placeholder="不选则为一级分类" clearable style="width:100%">
            <el-option v-for="c in catLevel1" :key="c.id" :label="c.cateName" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="排序"><el-input-number v-model="catf.sortOrder" :min="0" style="width:100%" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="catDia=false">取消</el-button>
        <el-button type="primary" @click="saveCat">保存</el-button>
      </template>
    </el-dialog>

    <!-- 商品弹窗 -->
    <el-dialog v-model="gDia" :title="gf.id?'编辑商品':'新增商品'" width="500px" :close-on-click-modal="false">
      <el-form :model="gf" label-width="80px">
        <el-form-item label="所属分类">
          <el-select v-model="gf.cateId" placeholder="请选择二级分类" style="width:100%">
            <el-option v-for="c in catLevel2" :key="c.id" :label="c.cateName" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="商品名"><el-input v-model="gf.goodsName" placeholder="请输入商品名称" /></el-form-item>
        <el-form-item label="价格"><el-input-number v-model="gf.price" :precision="2" :min="0" style="width:100%" /></el-form-item>
        <el-form-item label="库存"><el-input-number v-model="gf.stock" :min="0" style="width:100%" /></el-form-item>
        <el-form-item label="图片URL"><el-input v-model="gf.goodsImg" placeholder="请输入图片地址" /></el-form-item>
        <el-form-item v-if="gf.goodsImg" label="预览">
          <img :src="resolveImg(gf.goodsImg)" style="max-width:200px;max-height:120px;border-radius:8px;object-fit:cover;border:1px solid #e2e8f0" @error="$event.target.style.display='none'" />
        </el-form-item>
        <el-form-item label="简介"><el-input v-model="gf.info" type="textarea" :rows="3" placeholder="商品文字描述" /></el-form-item>
        <el-form-item label="详情图">
          <el-input v-model="gf.detailImgs" placeholder="图片URL，多张用英文逗号分隔：url1,url2" />
          <span style="font-size:11px;color:#94a3b8;margin-top:4px;display:block;">多张图片用英文逗号分隔，将展示在商品详情页</span>
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="gf.status">
            <el-radio :label="1">上架</el-radio>
            <el-radio :label="0">下架</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="gDia=false">取消</el-button>
        <el-button type="primary" @click="saveG">保存</el-button>
      </template>
    </el-dialog>

    <!-- 优惠券弹窗 -->
    <el-dialog v-model="cpDia" :title="cpf.id?'编辑优惠券':'新增优惠券'" width="440px" :close-on-click-modal="false">
      <el-form :model="cpf" label-width="90px">
        <el-form-item label="券名称"><el-input v-model="cpf.title" placeholder="如：新用户5元券" /></el-form-item>
        <el-form-item label="优惠金额"><el-input-number v-model="cpf.amount" :precision="2" :min="0" style="width:100%" /></el-form-item>
        <el-form-item label="最低消费"><el-input-number v-model="cpf.minAmount" :precision="2" :min="0" style="width:100%" /></el-form-item>
        <el-form-item label="库存"><el-input-number v-model="cpf.stock" :min="0" style="width:100%" /></el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="cpf.status">
            <el-radio :label="1">上架</el-radio>
            <el-radio :label="0">下架</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="cpDia=false">取消</el-button>
        <el-button type="primary" @click="saveCp">保存</el-button>
      </template>
    </el-dialog>

    <!-- 通知弹窗 -->
    <el-dialog v-model="nDia" :title="nf.id?'编辑通知':'新增通知'" width="480px" :close-on-click-modal="false">
      <el-form :model="nf" label-width="80px">
        <el-form-item label="通知标题"><el-input v-model="nf.title" placeholder="请输入通知标题" /></el-form-item>
        <el-form-item label="通知内容"><el-input v-model="nf.content" type="textarea" :rows="3" placeholder="选填" /></el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="nf.status">
            <el-radio :label="1">显示</el-radio>
            <el-radio :label="0">隐藏</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="nDia=false">取消</el-button>
        <el-button type="primary" @click="saveN">保存</el-button>
      </template>
    </el-dialog>

    <!-- 用户弹窗 -->
    <el-dialog v-model="uDia" title="修改用户" width="420px" :close-on-click-modal="false">
      <el-form :model="uf" label-width="70px">
        <el-form-item label="账号"><el-input v-model="uf.username" /></el-form-item>
        <el-form-item label="昵称"><el-input v-model="uf.nickName" /></el-form-item>
        <el-form-item label="手机号"><el-input v-model="uf.phone" /></el-form-item>
        <el-form-item label="邮箱"><el-input v-model="uf.email" /></el-form-item>
        <el-form-item label="头像URL"><el-input v-model="uf.avatar" placeholder="请输入头像图片地址" /></el-form-item>
        <el-form-item v-if="uf.avatar" label="预览">
          <img :src="uf.avatar" style="max-width:120px;max-height:80px;border-radius:8px;object-fit:cover;border:1px solid #e2e8f0" @error="$event.target.style.display='none'" />
        </el-form-item>
        <el-form-item label="新密码"><el-input v-model="uf.password" type="password" placeholder="不填则不改" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="uDia=false">取消</el-button>
        <el-button type="primary" @click="saveU">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import * as echarts from 'echarts'
import http, { resolveImg } from '@/utils/http'

const router = useRouter()
const folded = ref(false)
const cur = ref('dashboard')
const glist = ref([]); const ulist = ref([]); const olist = ref([]); const nlist = ref([])
const gDia = ref(false); const uDia = ref(false); const nDia = ref(false)
const gf = ref({ cateId:1, goodsName:'', price:0, stock:0, goodsImg:'', info:'', status:1 })
const uf = ref({ id:'', username:'', nickName:'', phone:'', email:'', password:'' })
const nf = ref({ id:'', title:'', content:'', status:1 })
const couponList = ref([]); const cpDia = ref(false)
const cpf = ref({ id:'', title:'', amount:0, minAmount:0, stock:100, status:1 })
/** 分类管理 */
const catList = ref([]); const catDia = ref(false)
const catf = ref({ id:'', cateName:'', icon:'', parentId:0, sortOrder:0 })
/** 树形数据：一级带子分类 */
const catTree = computed(() => {
  const l1List = catList.value.filter(c => c.level === 1)
  return l1List.map(l1 => ({
    ...l1,
    children: catList.value.filter(c => c.level === 2 && c.parentId === l1.id)
  }))
})
/** 二级分类（商品关联用） */
const catLevel2 = computed(() => catList.value.filter(c => c.level === 2))
/** 展开状态 */
const catOpen = reactive({})
/** 二级分类下商品展开 */
const catGoodsOpen = reactive({})
const toggleCatGoods = (id) => { catGoodsOpen[id] = !catGoodsOpen[id] }
/** 获取商品缩略图 */
const getGoodsThumb = (g) => resolveImg(g.goodsImg) || ''
/** 获取某二级分类下的商品 */
const getCatGoods = (cateId) => glist.value.filter(g => g.cateId === cateId)
/** 获取二级分类下第一个商品图片 */
const getCatFirstImg = (cateId) => {
  const goods = getCatGoods(cateId)
  return goods.length ? resolveImg(goods[0].goodsImg) : ''
}
/** 获取某二级分类下的商品数 */
const getCatGoodsCount = (cateId) => getCatGoods(cateId).length

const loadCat = async () => { const r = await http.get('/cate/admin/list'); catList.value = r.data || [] }
const toggleCat = (id) => { catOpen[id] = !catOpen[id] }
const openCat = (row) => { catf.value = row ? {...row} : { id:'', cateName:'', icon:'', parentId:0, sortOrder:0 }; catDia.value = true }
/** 添加子分类：预设父级ID */
const addChild = (parentId) => { catf.value = { id:'', cateName:'', parentId, sortOrder:0 }; catDia.value = true }
const saveCat = async () => {
  if (!catf.value.cateName.trim()) return ElMessage.warning('请输入分类名称')
  try {
    const u = catf.value.id ? '/cate/admin/update' : '/cate/admin/add'
    const r = await http.post(u, catf.value)
    if (r.data.code === 200) { ElMessage.success('已保存'); catDia.value = false; loadCat() }
    else ElMessage.error('保存失败')
  } catch { ElMessage.error('网络错误') }
}
const delCat = (id) => { ElMessageBox.confirm('删除该分类会同时影响其子分类和商品','提示',{ type:'warning' }).then(async () => { await http.post('/cate/admin/del', null, { params:{ id } }); ElMessage.success('已删除'); loadCat() }).catch(()=>{}) }
const aname = ref('admin')
const avt = ref('https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png')

const menu = [
  { k:'dashboard', icon:'📊', label:'数据中心' },
  { k:'goods', icon:'📦', label:'商品管理' },
  { k:'user', icon:'👥', label:'用户管理' },
  { k:'order', icon:'📋', label:'订单列表' },
  { k:'notify', icon:'🔔', label:'通知管理' },
  { k:'coupon', icon:'🎫', label:'优惠卡券' },
  { k:'category', icon:'📂', label:'分类管理' }
]
const qacts = ['批量上货','添加商品','添加分类','商品列表','订单列表','用户列表','支付配置','模板配置','监控中心']
const stats = ref([
  { icon:'📈', label:'今日新增', value:0, trend:true, cls:'s1' },
  { icon:'✅', label:'今日签到', value:0, trend:false, cls:'s2' },
  { icon:'💬', label:'待审评论', value:0, trend:false, cls:'s3' },
  { icon:'📌', label:'待办事项', value:0, trend:true, cls:'s4' },
  { icon:'🏆', label:'今日积分', value:4968, trend:true, cls:'s5' },
  { icon:'💰', label:'成功提现', value:0, trend:false, cls:'s6' }
])
const hot = ref([])

/** 加载热销排行（真实订单数据） */
const loadHot = async () => {
  try {
    const r = await http.get('/admin/hotRank')
    if (Array.isArray(r.data)) hot.value = r.data
  } catch { hot.value = [] }
}

const curName = computed(() => (menu.find(m => m.k === cur.value) || {}).label || '')
const chart = ref(null); let ci = null

const fmt = (r,c,v) => v?.replace('T',' ').substring(0,16) || ''
/** 商品名映射 */
const getGoodsName = (id) => (glist.value.find(g => g.id === id) || {}).goodsName || '商品#'+id
/** 用户名映射 */
const getUserName = (id) => (ulist.value.find(u => u.id === id) || {}).nickName || (ulist.value.find(u => u.id === id) || {}).username || '用户#'+id
/** 支付方式 */
const payLabel2 = (m) => ({ alipay:'支付宝', wechat:'微信', balance:'余额' }[m] || m || '-')
/** 订单状态 */
const stxt2 = (s) => ({ completed:'已完成', refunded:'已退款', pending:'待付款' }[s] || s || '已完成')

const loadG = async () => { const r = await http.get('/admin/goods/list'); glist.value = r.data || [] }
const loadU = async () => { const r = await http.get('/admin/user/list'); ulist.value = r.data || []; stats.value[0].value = ulist.value.length }
const orderTotal = ref(0); const orderPage = ref(1); const orderPageSize = ref(10)
const loadO = async () => {
  const r = await http.get('/admin/order/all', { params: { page: orderPage.value, pageSize: orderPageSize.value } })
  olist.value = r.data.list || []; orderTotal.value = r.data.total || 0
}
const loadN = async () => { const r = await http.get('/notify/admin/list'); nlist.value = r.data || [] }
const loadCp = async () => { const r = await http.get('/coupon/admin/list'); couponList.value = r.data || [] }

const openG = (row) => { gf.value = row ? {...row} : { cateId:1, goodsName:'', price:0, stock:0, goodsImg:'', info:'', status:1 }; gDia.value = true }
/** 在指定二级分类下新增商品 */
const addGoodsToCate = (cateId) => { gf.value = { cateId, goodsName:'', price:0, stock:0, goodsImg:'', info:'', detailImgs:'', status:1 }; gDia.value = true }
const saveG = async () => {
  if (!gf.value.goodsName.trim()) return ElMessage.warning('请输入商品名称')
  try {
    const u = gf.value.id ? '/admin/goods/update' : '/admin/goods/add'
    const r = await http.post(u, gf.value)
    if (r.data.code === 200 || r.data === true || r.data.success) { ElMessage.success('已保存'); gDia.value = false; loadG() }
    else ElMessage.error(r.data?.msg || '保存失败')
  } catch { ElMessage.error('网络错误，请稍后重试') }
}
const delG = (id) => { ElMessageBox.confirm('确定删除该商品？','提示',{ type:'warning', confirmButtonText:'确认删除', cancelButtonText:'取消' }).then(async () => { await http.post('/admin/goods/del', null, { params:{ id } }); ElMessage.success('已删除'); loadG() }).catch(()=>{}) }
const openU = (row) => { uf.value = {...row, password:''}; uDia.value = true }
const saveU = async () => {
  if (!uf.value.username.trim()) return ElMessage.warning('账号不能为空')
  try {
    const r = await http.post('/admin/user/update', uf.value)
    if (r.data.code === 200 || r.data === true || r.data.success) { ElMessage.success('已更新'); uDia.value = false; loadU() }
    else ElMessage.error(r.data?.msg || '保存失败')
  } catch { ElMessage.error('网络错误，请稍后重试') }
}
const delU = (id) => { ElMessageBox.confirm('确定删除该用户？','提示',{ type:'warning', confirmButtonText:'确认删除', cancelButtonText:'取消' }).then(async () => { await http.post('/admin/user/del', null, { params:{ id } }); ElMessage.success('已删除'); loadU() }).catch(()=>{}) }

const openN = (row) => { nf.value = row ? {...row} : { id:'', title:'', content:'', status:1 }; nDia.value = true }
const saveN = async () => {
  if (!nf.value.title.trim()) return ElMessage.warning('请输入通知标题')
  try {
    const u = nf.value.id ? '/notify/admin/update' : '/notify/admin/add'
    const r = await http.post(u, nf.value)
    if (r.data.code === 200) { ElMessage.success('已保存'); nDia.value = false; loadN() }
    else ElMessage.error('保存失败')
  } catch { ElMessage.error('网络错误') }
}
const delN = (id) => { ElMessageBox.confirm('确定删除该通知？','提示',{ type:'warning', confirmButtonText:'确认删除', cancelButtonText:'取消' }).then(async () => { await http.post('/notify/admin/del', null, { params:{ id } }); ElMessage.success('已删除'); loadN() }).catch(()=>{}) }
const openCp = (row) => { cpf.value = row ? {...row} : { id:'', title:'', amount:0, minAmount:0, stock:100, status:1 }; cpDia.value = true }
const saveCp = async () => {
  if (!cpf.value.title.trim()) return ElMessage.warning('请输入券名称')
  try {
    const u = cpf.value.id ? '/coupon/admin/update' : '/coupon/admin/add'
    const r = await http.post(u, cpf.value)
    if (r.data.code === 200) { ElMessage.success('已保存'); cpDia.value = false; loadCp() }
    else ElMessage.error('保存失败')
  } catch { ElMessage.error('网络错误') }
}
const delCp = (id) => { ElMessageBox.confirm('确定删除该优惠券？','提示',{ type:'warning', confirmButtonText:'确认删除', cancelButtonText:'取消' }).then(async () => { await http.post('/coupon/admin/del', null, { params:{ id } }); ElMessage.success('已删除'); loadCp() }).catch(()=>{}) }
const toggleN = async (row) => {
  row.status = row.status === 1 ? 0 : 1
  await http.post('/notify/admin/update', row)
  loadN()
}

const logout = () => { http.post('/admin/logout').then(() => router.push('/adminLogin')) }

watch(() => cur.value, (v) => { if (v === 'dashboard') setTimeout(drawChart, 100) })

const drawChart = async () => {
  if (!chart.value) return
  // 从后端获取真实趋势数据
  let d = { dates:[], orders:[], amount:[], reg:[] }
  try { const r = await http.get('/admin/trend'); if (r.data) d = r.data } catch {}
  ci = echarts.init(chart.value)
  ci.setOption({
    tooltip:{ trigger:'axis', backgroundColor:'#fff', borderColor:'#e2e8f0', textStyle:{ color:'#0f172a' } },
    legend:{ data:['订单数','交易额(元)','注册数'], bottom:0, textStyle:{ color:'#64748b', fontSize:11 } },
    grid:{ top:10, left:50, right:20, bottom:40, containLabel:true },
    xAxis:{ type:'category', data: d.dates.length ? d.dates : ['暂无数据'], axisLine:{ lineStyle:{ color:'#e2e8f0' } }, axisLabel:{ color:'#94a3b8', fontSize:11 } },
    yAxis:{ type:'value', splitLine:{ lineStyle:{ color:'#f1f5f9' } }, axisLabel:{ color:'#94a3b8', fontSize:11 } },
    series:[
      { name:'订单数', type:'line', smooth:true, data: d.orders, lineStyle:{ color:'#f43f5e', width:3 }, itemStyle:{ color:'#f43f5e' }, symbol:'circle', symbolSize:6, areaStyle:{ color: new echarts.graphic.LinearGradient(0,0,0,1,[{offset:0,color:'rgba(244,63,94,0.08)'},{offset:1,color:'rgba(244,63,94,0)'}]) } },
      { name:'交易额(元)', type:'line', smooth:true, data: d.amount, lineStyle:{ color:'#3b82f6', width:3 }, itemStyle:{ color:'#3b82f6' }, symbol:'diamond', symbolSize:6, areaStyle:{ color: new echarts.graphic.LinearGradient(0,0,0,1,[{offset:0,color:'rgba(59,130,246,0.08)'},{offset:1,color:'rgba(59,130,246,0)'}]) } },
      { name:'注册数', type:'line', smooth:true, data: d.reg, lineStyle:{ color:'#10b981', width:3 }, itemStyle:{ color:'#10b981' }, symbol:'triangle', symbolSize:6, areaStyle:{ color: new echarts.graphic.LinearGradient(0,0,0,1,[{offset:0,color:'rgba(16,185,129,0.08)'},{offset:1,color:'rgba(16,185,129,0)'}]) } }
    ]
  })
  window.addEventListener('resize', () => ci?.resize())
}

onMounted(async () => {
  await Promise.all([loadG(), loadU(), loadO(), loadN(), loadCp(), loadCat(), loadHot()])
  if (cur.value === 'dashboard') drawChart()
})
</script>

<style scoped>
* { margin:0; padding:0; box-sizing:border-box; }
.layout { display:flex; min-height:100vh; background:#f1f5f9; }

/* ====== 侧边栏 ====== */
.side {
  width:250px; background:#fff;
  border-right:1px solid #e2e8f0;
  display:flex; flex-direction:column; transition:width 0.3s;
  flex-shrink:0; z-index:20;
}
.side.fold { width:72px; }
.logo {
  display:flex; align-items:center; gap:10px; padding:20px 18px;
  cursor:pointer; border-bottom:1px solid #f1f5f9;
}
.logo-icon { font-size:30px; }
.lt {
  font-size:17px; font-weight:800;
  background:linear-gradient(135deg, #6366f1, #8b5cf6);
  -webkit-background-clip:text; background-clip:text; color:transparent;
  letter-spacing:1px;
}

.nav { flex:1; padding:14px 10px; }
.mi {
  display:flex; align-items:center; gap:10px; padding:12px 14px;
  border-radius:12px; cursor:pointer; font-size:14px;
  color:#64748b; transition:all 0.2s; margin-bottom:2px;
  position:relative;
}
.mic { font-size:18px; flex-shrink:0; }
.mil { flex:1; }
.mi-dot { width:6px; height:6px; border-radius:50%; background:#6366f1; margin-left:auto; }
.mi:hover { background:#f8fafc; color:#0f172a; }
.mi.on { background:rgba(99,102,241,0.06); color:#6366f1; font-weight:600; }

.sbot { padding:14px; border-top:1px solid #f1f5f9; }
.urow { display:flex; align-items:center; gap:10px; margin-bottom:12px; padding:8px 10px; background:#f8fafc; border-radius:12px; }
.uavt { width:36px; height:36px; border-radius:50%; }
.un { font-size:13px; font-weight:600; color:#0f172a; }
.ur { font-size:11px; color:#94a3b8; }
.lbtn {
  width:100%; display:flex; align-items:center; justify-content:center;
  gap:6px; padding:9px; border:none; border-radius:10px;
  background:rgba(244,63,94,0.05); color:#f43f5e;
  cursor:pointer; font-size:13px; font-weight:500; transition:all 0.2s;
}
.lbtn:hover { background:rgba(244,63,94,0.1); }

/* ====== 顶栏 ====== */
.main { flex:1; display:flex; flex-direction:column; overflow:hidden; }
.bar {
  height:60px; background:#fff; border-bottom:1px solid #e2e8f0;
  display:flex; align-items:center; padding:0 24px; gap:16px;
}
.fbtn {
  background:none; border:none; font-size:20px; cursor:pointer;
  color:#94a3b8; transition:color 0.2s; width:36px; height:36px;
  border-radius:8px; display:flex; align-items:center; justify-content:center;
}
.fbtn:hover { color:#6366f1; background:#f8fafc; }
.br { margin-left:auto; display:flex; align-items:center; gap:18px; }
.bell { font-size:18px; cursor:pointer; position:relative; }
.bdot { position:absolute; top:-2px; right:-3px; width:8px; height:8px; background:#f43f5e; border-radius:50%; border:2px solid #fff; }
.abadge {
  padding:5px 14px; border-radius:20px;
  background:rgba(99,102,241,0.06); color:#6366f1;
  font-size:12px; font-weight:600;
}

/* ====== 内容区 ====== */
.content { flex:1; padding:24px; overflow-y:auto; }

/* 统计卡片 */
.sg { display:grid; grid-template-columns:repeat(auto-fit, minmax(180px, 1fr)); gap:14px; margin-bottom:20px; }
.sc {
  background:#fff; border-radius:16px; padding:20px;
  display:flex; align-items:center; gap:16px;
  border:1px solid #e2e8f0; transition:all 0.25s;
  box-shadow:0 1px 2px rgba(0,0,0,0.02);
}
.sc:hover { transform:translateY(-2px); box-shadow:0 8px 24px rgba(0,0,0,0.06); border-color:transparent; }
.sc-icon {
  width:48px; height:48px; border-radius:14px;
  display:flex; align-items:center; justify-content:center; flex-shrink:0;
}
.sc-icon span { font-size:22px; }
.s1 .sc-icon { background:rgba(99,102,241,0.08); }
.s2 .sc-icon { background:rgba(16,185,129,0.08); }
.s3 .sc-icon { background:rgba(245,158,11,0.08); }
.s4 .sc-icon { background:rgba(59,130,246,0.08); }
.s5 .sc-icon { background:rgba(244,63,94,0.08); }
.s6 .sc-icon { background:rgba(14,165,233,0.08); }
.sc-info { flex:1; min-width:0; }
.sv { font-size:26px; font-weight:800; color:#0f172a; display:block; line-height:1.1; }
.sl { font-size:12px; color:#94a3b8; margin-top:2px; display:block; }
.sc-trend { color:#10b981; font-size:14px; font-weight:700; flex-shrink:0; }

/* 快捷操作 */
.qa {
  background:#fff; border-radius:16px; padding:18px 22px;
  margin-bottom:20px; border:1px solid #e2e8f0;
  box-shadow:0 1px 2px rgba(0,0,0,0.02);
}
.qa-head { display:flex; justify-content:space-between; align-items:center; margin-bottom:12px; }
.ql { font-size:14px; font-weight:600; color:#0f172a; }
.qa-hint { font-size:11px; color:#94a3b8; }
.qb { display:flex; flex-wrap:wrap; gap:8px; }
.qbtn {
  padding:7px 16px; border-radius:10px;
  background:#f8fafc; border:1px solid #e2e8f0;
  color:#475569; font-size:12px; cursor:pointer; font-weight:500;
  transition:all 0.2s;
}
.qbtn:hover { background:#fff; border-color:#6366f1; color:#6366f1; }

/* 图表+排行 */
.twocol { display:grid; grid-template-columns:2fr 1fr; gap:16px; }
@media (max-width:960px) { .twocol { grid-template-columns:1fr; } }

.pn {
  background:#fff; border-radius:16px; padding:22px;
  border:1px solid #e2e8f0; box-shadow:0 1px 2px rgba(0,0,0,0.02);
}
.ph { display:flex; justify-content:space-between; align-items:flex-start; margin-bottom:18px; }
.ph h3 { font-size:16px; font-weight:700; color:#0f172a; }
.ph-sub { font-size:11px; color:#94a3b8; margin-top:3px; }
.ch { height:320px; }

.lg { display:flex; gap:16px; }
.lg-item { font-size:11px; color:#94a3b8; display:flex; align-items:center; gap:5px; }
.lg-item i { display:inline-block; width:8px; height:8px; border-radius:50%; }

/* 分类树形 */
.cat-tree { display: flex; flex-direction: column; gap: 4px; }
.ct-node { background: #fff; border: 1px solid #e2e8f0; border-radius: 12px; overflow: hidden; }
.ct-row { display: flex; align-items: center; gap: 8px; padding: 12px 14px; cursor: pointer; transition: background 0.15s; }
.ct-row:hover { background: #f8fafc; }
.ct-l1 { border-bottom: 1px solid #f1f5f9; }
.ct-l2 { padding-left: 40px; background: #f1f5f9; }
.ct-l2 .ct-name { color: #6366f1; font-weight: 600; }
.ct-arrow { font-size: 10px; color: #94a3b8; width: 14px; flex-shrink: 0; }
.ct-dot { color: #94a3b8; font-weight: 700; flex-shrink: 0; font-size: 14px; }
.ct-icon { font-size: 16px; flex-shrink: 0; }
.ct-name { font-size: 14px; font-weight: 600; color: #0f172a; flex: 1; }
.ct-tag { font-size: 10px; padding: 2px 6px; border-radius: 6px; background: rgba(99,102,241,0.08); color: #6366f1; font-weight: 600; }
.ct-tag-l2 { background: rgba(16,185,129,0.08); color: #10b981; }
.ct-info { font-size: 11px; color: #94a3b8; }
.ct-actions { display: flex; gap: 4px; flex-shrink: 0; }
.ct-children { background: #fafafa; }
.ct-empty { padding: 14px 40px; color: #94a3b8; font-size: 13px; }
.ct-gap { width: 28px; flex-shrink: 0; }
.ct-goods { background: #e2e8f0; }
.ct-goods-row { padding: 8px 14px; }

/* 热销排行 */
.hl { display:flex; flex-direction:column; gap:6px; }
.hi {
  display:flex; align-items:center; gap:12px;
  padding:10px 14px; border-radius:12px; transition:background 0.15s;
}
.hi:hover { background:#f8fafc; }
.rk {
  width:30px; height:30px; border-radius:10px;
  display:flex; align-items:center; justify-content:center;
  font-weight:800; font-size:13px; flex-shrink:0;
}
.r1 { background:rgba(245,158,11,0.15); color:#f59e0b; }
.r2 { background:rgba(148,163,184,0.12); color:#64748b; }
.r3 { background:rgba(244,63,94,0.1); color:#f43f5e; }
.hi-info { flex:1; min-width:0; }
.hn { font-size:13px; font-weight:500; color:#0f172a; display:block; margin-bottom:4px; }
.hi-bar { height:4px; border-radius:2px; background:#f1f5f9; overflow:hidden; }
.hi-fill { height:100%; border-radius:2px; background:linear-gradient(90deg, #6366f1, #8b5cf6); transition:width 0.6s; }
.ho { color:#f43f5e; font-weight:700; font-size:13px; flex-shrink:0; }

@media (max-width:768px) {
  .side { position:fixed; top:0; left:0; bottom:0; transform:translateX(0); transition:transform 0.3s; }
  .side.fold { transform:translateX(-100%); }
  .sg { grid-template-columns:repeat(2, 1fr); }
  .content { padding:14px; }
}
</style>
