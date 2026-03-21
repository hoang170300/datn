<template>
    <div style="height:calc(100vh - 80px);display:flex;flex-direction:column;overflow:hidden;">
   
      <!-- Header + Tab bar -->
      <div class="d-flex align-items-center gap-3 px-3 pt-3 pb-0"
        style="background:#fff;border-bottom:2px solid #f0f0f0;flex-shrink:0;">
        <div class="fw-bold d-flex align-items-center gap-2" style="color:#1a1a2e;">
          <span style="font-size:1.3rem;"></span> BÁN HÀNG TẠI QUẦY
          <span class="badge rounded-pill ms-1" style="background:#22c55e;font-size:0.65rem;">OFFLINE</span>
        </div>
   
        <!-- Tabs -->
        <div class="d-flex gap-1 ms-3 overflow-auto" style="flex:1;">
          <div v-for="(tab,i) in tabs" :key="tab.id"
            class="d-flex align-items-center gap-2 px-3 py-2 rounded-top-3"
            style="cursor:pointer;white-space:nowrap;min-width:120px;border:1px solid;border-bottom:none;transition:all 0.15s;"
            :style="activeTab===i
              ? 'background:#e91e8c;color:#fff;border-color:#e91e8c;'
              : 'background:#f8f9fa;color:#555;border-color:#dee2e6;'"
            @click="activeTab=i">
            <span style="font-size:0.8rem;"></span>
            <span style="font-size:0.82rem;font-weight:600;">Đơn {{ i+1 }}</span>
            <span v-if="tab.cartItems.length"
              class="badge rounded-pill"
              :style="activeTab===i ? 'background:rgba(255,255,255,0.3);' : 'background:#e91e8c;color:#fff;'"
              style="font-size:0.6rem;padding:2px 6px;">
              {{ tab.cartItems.length }}
            </span>
            <span v-if="tabs.length>1"
              class="ms-auto"
              style="font-size:0.7rem;opacity:0.7;"
              @click.stop="closeTab(i)">✕</span>
          </div>
   
          <!-- Nút thêm tab -->
          <button class="btn btn-sm rounded-top-3 px-3"
            style="border:1px dashed #dee2e6;border-bottom:none;background:#fff;color:#e91e8c;font-size:0.82rem;"
            @click="addTab">
            + Đơn mới
          </button>
        </div>
      </div>
   
      <!-- Main POS area - flex row -->
      <div class="d-flex flex-grow-1" style="overflow:hidden;">
   
        <!-- LEFT: Product list -->
        <div class="d-flex flex-column" style="flex:1;overflow:hidden;border-right:1px solid #f0f0f0;">
          <!-- Search bar -->
          <div class="p-3 pb-2" style="flex-shrink:0;background:#fff;">
            <div class="input-group">
              <span class="input-group-text bg-white"><i class="bi bi-search text-muted"></i></span>
              <input type="text" class="form-control border-start-0"
                v-model="keyword"
                placeholder="Tim san pham, nhan vat... (de trong = tat ca)"
                @input="searchDebounce">
              <button v-if="keyword" class="btn btn-outline-secondary"
                @click="keyword='';doSearch()">
                <i class="bi bi-x"></i>
              </button>
            </div>
          </div>
   
          <!-- Product grid -->
          <div class="p-3 pt-1" style="flex:1;overflow-y:auto;">
            <div v-if="searching" class="text-center py-5">
              <div class="spinner-border" style="color:#e91e8c;"></div>
            </div>
            <div v-else-if="!products.length" class="text-center py-5 text-muted">
              <i class="bi bi-search fs-1 d-block mb-2 opacity-25"></i>
              Khong co san pham nao
            </div>
            <div v-else class="row g-2">
              <div v-for="p in products" :key="p.id" class="col-6 col-xl-4">
                <div class="card border h-100"
                  style="border-radius:12px;cursor:pointer;transition:all 0.15s;"
                  @mouseenter="$event.currentTarget.style.borderColor='#e91e8c'"
                  @mouseleave="$event.currentTarget.style.borderColor=''"
                  @click="openProductModal(p)">
                  <img :src="p.primaryImageUrl || '/img/placeholder.svg'"
                    class="card-img-top"
                    style="height:110px;object-fit:cover;border-radius:12px 12px 0 0;"
                    @error="$event.target.src='/img/placeholder.svg'">
                  <div class="card-body p-2">
                    <div class="small fw-semibold lh-sm mb-1"
                      style="display:-webkit-box;-webkit-line-clamp:2;-webkit-box-orient:vertical;overflow:hidden;">
                      {{ p.name }}
                    </div>
                    <div class="d-flex gap-1 flex-wrap mb-1">
                      <span class="badge rounded-pill"
                        :style="p.productType==='RENTAL' ? 'background:#0ea5e9;' : p.productType==='BOTH' ? 'background:#8b5cf6;' : 'background:#e91e8c;'"
                        style="font-size:0.6rem;color:#fff;">
                        {{ p.productType==='RENTAL' ? 'Thue' : p.productType==='BOTH' ? 'Mua/Thue' : 'Mua' }}
                      </span>
                    </div>
                    <div v-if="p.salePrice" class="small fw-bold" style="color:#e91e8c;">
                      {{ formatPrice(p.salePrice) }}
                    </div>
                    <div v-if="p.rentalPricePerDay" class="small text-info">
                      {{ formatPrice(p.rentalPricePerDay) }}/ngay
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
   
        <!-- RIGHT: Cart + Payment -->
        <div class="d-flex flex-column" style="width:380px;flex-shrink:0;background:#fafafa;overflow:hidden;">
   
          <!-- Customer info -->
          <div class="p-3 pb-2" style="flex-shrink:0;background:#fff;border-bottom:1px solid #f0f0f0;">
            <div class="d-flex align-items-center gap-2 mb-2">
              <i class="bi bi-person text-muted small"></i>
              <span class="small fw-semibold text-muted">Thong tin khach hang</span>
            </div>
            <div class="d-flex gap-2">
              <input class="form-control form-control-sm" v-model="currentTab.customerName"
                placeholder="Ten khach (tuy chon)">
              <input class="form-control form-control-sm" type="tel"
                v-model="currentTab.customerPhone" placeholder="So dien thoai">
            </div>
          </div>
   
          <!-- Cart items -->
          <div style="flex:1;overflow-y:auto;" class="p-3 pb-1">
            <div v-if="!currentTab.cartItems.length"
              class="text-center py-5 text-muted">
              <i class="bi bi-bag fs-1 d-block mb-2 opacity-25"></i>
              <div class="small">Chon san pham tu danh sach</div>
            </div>
   
            <div v-for="(item,i) in currentTab.cartItems" :key="i"
              class="card border-0 shadow-sm mb-2"
              style="border-radius:10px;">
              <div class="card-body p-2">
                <div class="d-flex gap-2">
                  <div class="flex-grow-1">
                    <div class="small fw-semibold lh-sm">{{ item.productName }}</div>
                    <div class="d-flex gap-1 align-items-center mt-1 flex-wrap">
                      <span class="badge rounded-pill"
                        style="font-size:0.6rem;background:#e91e8c22;color:#e91e8c;">
                        {{ item.size }}
                      </span>
                      <span v-if="item.color" class="text-muted" style="font-size:0.72rem;">
                        {{ item.color }}
                      </span>
                      <span class="badge rounded-pill"
                        :style="item.orderType==='RENTAL' ? 'background:#0ea5e922;color:#0ea5e9;' : 'background:#22c55e22;color:#16a34a;'"
                        style="font-size:0.6rem;">
                        {{ item.orderType==='RENTAL' ? 'Thue' : 'Mua' }}
                      </span>
                    </div>
                    <!-- Ngay thue -->
                    <div v-if="item.orderType==='RENTAL'" class="mt-1 d-flex gap-1 align-items-center">
                      <input type="date" class="form-control form-control-sm py-0"
                        style="font-size:0.72rem;width:120px;"
                        v-model="item.rentalStartDate">
                      <span class="small text-muted">-</span>
                      <input type="date" class="form-control form-control-sm py-0"
                        style="font-size:0.72rem;width:120px;"
                        v-model="item.rentalEndDate">
                      <span class="small text-info fw-semibold">
                        {{ rentalDays(item) }}ng
                      </span>
                    </div>
                  </div>
                  <!-- Quantity + delete -->
                  <div class="d-flex flex-column align-items-end gap-1">
                    <button class="btn btn-sm btn-outline-danger rounded-circle p-0"
                      style="width:22px;height:22px;font-size:0.65rem;line-height:1;"
                      @click="currentTab.cartItems.splice(i,1)">
                      <i class="bi bi-x"></i>
                    </button>
                    <div class="d-flex align-items-center gap-1">
                      <button class="btn btn-sm btn-outline-secondary rounded-circle p-0"
                        style="width:22px;height:22px;font-size:0.7rem;"
                        @click="item.quantity>1?item.quantity--:currentTab.cartItems.splice(i,1)">
                        -
                      </button>
                      <span class="small fw-bold" style="min-width:20px;text-align:center;">
                        {{ item.quantity }}
                      </span>
                      <button class="btn btn-sm btn-outline-secondary rounded-circle p-0"
                        style="width:22px;height:22px;font-size:0.7rem;"
                        @click="item.quantity++">
                        +
                      </button>
                    </div>
                    <div class="small fw-bold" style="color:#e91e8c;">
                      {{ formatPrice(itemTotal(item)) }}
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
   
          <!-- Payment section -->
          <div v-if="currentTab.cartItems.length"
            class="p-3" style="flex-shrink:0;background:#fff;border-top:2px solid #f0f0f0;">
   
            <!-- Total -->
            <div class="d-flex justify-content-between small mb-1">
              <span class="text-muted">Tieu tong</span>
              <span>{{ formatPrice(currentSubtotal) }}</span>
            </div>
            <div v-if="currentDeposit>0" class="d-flex justify-content-between small mb-1">
              <span style="color:#f59e0b;">Tien coc thue</span>
              <span style="color:#f59e0b;">{{ formatPrice(currentDeposit) }}</span>
            </div>
            <div class="d-flex justify-content-between fw-bold mb-3">
              <span>Tong cong</span>
              <span style="color:#e91e8c;font-size:1.1rem;">{{ formatPrice(currentGrandTotal) }}</span>
            </div>
   
            <!-- Phuong thuc thanh toan: 2 nut -->
            <div class="d-flex gap-2 mb-3">
              <button :class="['btn btn-sm flex-grow-1 fw-semibold rounded-pill',
                currentTab.payMethod==='COD' ? 'text-white' : 'btn-outline-secondary']"
                :style="currentTab.payMethod==='COD' ? 'background:#16a34a;' : ''"
                @click="currentTab.payMethod='COD'">
                <i class="bi bi-cash-coin me-1"></i>Tien mat
              </button>
              <button :class="['btn btn-sm flex-grow-1 fw-semibold rounded-pill',
                currentTab.payMethod==='BANK_TRANSFER' ? 'text-white' : 'btn-outline-secondary']"
                :style="currentTab.payMethod==='BANK_TRANSFER' ? 'background:#1d4ed8;' : ''"
                @click="currentTab.payMethod='BANK_TRANSFER'">
                <i class="bi bi-qr-code me-1"></i>QR / CK
              </button>
            </div>
   
            <!-- Chi tiet theo phuong thuc -->
   
            <!-- TIEN MAT: tinh tien thua -->
            <div v-if="currentTab.payMethod==='COD'"
              class="p-2 rounded-3 mb-3 small"
              style="background:#f0fdf4;border:1px solid #86efac;">
              <div class="fw-semibold text-success mb-2">
                <i class="bi bi-calculator me-1"></i>Tinh tien thua
              </div>
              <div class="input-group input-group-sm mb-2">
                <span class="input-group-text">Khach dua</span>
                <input type="number" class="form-control text-end fw-bold"
                  v-model.number="currentTab.cashReceived"
                  :min="currentGrandTotal" step="1000"
                  placeholder="Nhap so tien">
                <span class="input-group-text">d</span>
              </div>
              <!-- Goi y menh gia -->
              <div class="d-flex gap-1 flex-wrap mb-2">
                <button v-for="q in quickCash" :key="q"
                  class="btn btn-xs btn-outline-success rounded-pill"
                  style="font-size:0.68rem;padding:1px 8px;"
                  @click="currentTab.cashReceived=q">
                  {{ formatPrice(q) }}
                </button>
              </div>
              <div v-if="currentTab.cashReceived >= currentGrandTotal"
                class="d-flex justify-content-between fw-bold text-success">
                <span>Tien thua:</span>
                <span>{{ formatPrice(currentTab.cashReceived - currentGrandTotal) }}</span>
              </div>
              <div v-else-if="currentTab.cashReceived>0" class="text-danger small">
                <i class="bi bi-exclamation-triangle me-1"></i>
                Con thieu {{ formatPrice(currentGrandTotal - currentTab.cashReceived) }}
              </div>
            </div>
   
            <!-- QR CHUYEN KHOAN: hien STK + QR tĩnh -->
            <div v-if="currentTab.payMethod==='BANK_TRANSFER'"
              class="p-2 rounded-3 mb-3 small"
              style="background:#eff6ff;border:1px solid #93c5fd;">
              <div class="fw-semibold text-primary mb-2">
                <i class="bi bi-bank me-1"></i>Thong tin chuyen khoan
              </div>
              <div class="mb-1">NH: <strong>Vietcombank</strong></div>
              <div class="d-flex align-items-center gap-1 mb-1">
                STK: <strong class="text-primary ms-1">1234567890</strong>
                <button class="btn btn-sm btn-outline-secondary ms-1 py-0 px-1"
                  style="font-size:0.6rem;"
                  @click="copyText('1234567890')">
                  <i class="bi bi-copy"></i>
                </button>
              </div>
              <div class="mb-2">CHU TK: <strong>COSPLAY SHOP</strong></div>
              <!-- Ma QR tinh (dung img placeholder) -->
              <div class="text-center p-2 rounded-2" style="background:#fff;">
                <div class="fw-bold" style="color:#1d4ed8;font-size:1rem;">
                  {{ formatPrice(currentGrandTotal) }}
                </div>
                <div class="text-muted" style="font-size:0.72rem;">
                  Noi dung: POS {{ new Date().getTime().toString().slice(-6) }}
                </div>
                <div class="mt-1">
                  <i class="bi bi-qr-code" style="font-size:3rem;color:#1d4ed8;opacity:0.7;"></i>
                </div>
                <div class="small text-muted">QR Code</div>
              </div>
              <div class="mt-2 text-center small text-success fw-semibold">
                <i class="bi bi-check-circle me-1"></i>
                Xac nhan sau khi khach chuyen khoan thanh cong
              </div>
            </div>
   
            <!-- Ghi chu -->
            <textarea class="form-control form-control-sm mb-3" rows="2"
              v-model="currentTab.orderNote"
              placeholder="Ghi chu don hang..."></textarea>
   
            <!-- Nut tao don -->
            <button class="btn w-100 fw-bold text-white py-2"
              style="background:linear-gradient(135deg,#e91e8c,#c2185b);border-radius:10px;font-size:0.95rem;"
              :disabled="placing || (currentTab.payMethod==='COD' && currentTab.cashReceived>0 && currentTab.cashReceived<currentGrandTotal)"
              @click="placeOrder">
              <span v-if="placing" class="spinner-border spinner-border-sm me-2"></span>
              <i v-else class="bi bi-check-circle me-2"></i>
              {{ currentTab.payMethod==='COD' ? 'Thu tien & In hoa don' : 'Xac nhan & In hoa don' }}
            </button>
          </div>
   
          <!-- Gio hang rong -->
          <div v-else class="p-3 text-center text-muted" style="flex-shrink:0;">
            <div class="small">Chua co san pham trong don</div>
          </div>
        </div>
      </div>
   
      <!-- Modal chon variant -->
      <Teleport to="body">
        <div v-if="selectedProduct"
          class="position-fixed d-flex align-items-center justify-content-center"
          style="inset:0;background:rgba(0,0,0,0.5);z-index:3000;padding:16px;"
          @click.self="selectedProduct=null">
          <div class="card border-0 shadow-lg" style="width:100%;max-width:420px;border-radius:20px;">
            <div class="card-body p-4">
              <div class="d-flex align-items-start gap-3 mb-4">
                <img :src="selectedProduct.primaryImageUrl||'/img/placeholder.svg'"
                  class="rounded-3" style="width:70px;height:70px;object-fit:cover;">
                <div>
                  <h6 class="fw-bold mb-1">{{ selectedProduct.name }}</h6>
                  <div class="d-flex gap-1 flex-wrap">
                    <span v-if="selectedProduct.salePrice" class="small fw-bold" style="color:#e91e8c;">
                      {{ formatPrice(selectedProduct.salePrice) }}
                    </span>
                    <span v-if="selectedProduct.rentalPricePerDay" class="small text-info">
                      {{ formatPrice(selectedProduct.rentalPricePerDay) }}/ngay
                    </span>
                  </div>
                </div>
              </div>
   
              <!-- Chon mua / thue -->
              <div v-if="selectedProduct.productType==='BOTH'" class="mb-3">
                <label class="small fw-semibold mb-2 d-block">Hinh thuc:</label>
                <div class="d-flex gap-2">
                  <button :class="['btn btn-sm flex-grow-1 rounded-pill', pickType==='SALE'?'text-white':'btn-outline-secondary']"
                    :style="pickType==='SALE'?'background:#e91e8c;':''"
                    @click="pickType='SALE'">Mua</button>
                  <button :class="['btn btn-sm flex-grow-1 rounded-pill', pickType==='RENTAL'?'text-white':'btn-outline-secondary']"
                    :style="pickType==='RENTAL'?'background:#0ea5e9;':''"
                    @click="pickType='RENTAL'">Thue</button>
                </div>
              </div>
   
              <!-- Chon ngay thue -->
              <div v-if="pickType==='RENTAL'" class="mb-3">
                <label class="small fw-semibold mb-1 d-block">Ngay thue:</label>
                <div class="d-flex gap-2">
                  <div class="flex-grow-1">
                    <small class="text-muted">Tu ngay</small>
                    <input type="date" class="form-control form-control-sm" v-model="pickStartDate">
                  </div>
                  <div class="flex-grow-1">
                    <small class="text-muted">Den ngay</small>
                    <input type="date" class="form-control form-control-sm" v-model="pickEndDate">
                  </div>
                </div>
                <div v-if="pickStartDate && pickEndDate" class="mt-1 small text-info fw-semibold">
                  {{ pickDays }} ngay x {{ formatPrice(selectedProduct.rentalPricePerDay) }}
                  = {{ formatPrice(pickDays * selectedProduct.rentalPricePerDay) }}
                </div>
              </div>
   
              <!-- Chon size -->
              <div class="mb-3">
                <label class="small fw-semibold mb-2 d-block">Chon size:</label>
                <div class="d-flex gap-2 flex-wrap">
                  <button v-for="v in (selectedProduct.variants||[])" :key="v.id"
                    :class="['btn btn-sm rounded-3', pickVariant?.id===v.id?'text-white':'btn-outline-secondary']"
                    :style="pickVariant?.id===v.id?'background:#e91e8c;':''"
                    :disabled="pickType==='SALE'?v.stockQuantity<=0:v.rentalQuantity<=0"
                    @click="pickVariant=v">
                    {{ v.size }}
                    <span v-if="v.color" class="small d-block" style="font-size:0.6rem;">{{ v.color }}</span>
                    <span class="small d-block" style="font-size:0.6rem;"
                      :class="(pickType==='SALE'?v.stockQuantity:v.rentalQuantity)<=0?'text-danger':''">
                      Con: {{ pickType==='SALE'?v.stockQuantity:v.rentalQuantity }}
                    </span>
                  </button>
                </div>
              </div>
   
              <!-- So luong -->
              <div class="mb-4 d-flex align-items-center gap-3">
                <label class="small fw-semibold">So luong:</label>
                <div class="d-flex align-items-center gap-2">
                  <button class="btn btn-sm btn-outline-secondary rounded-circle"
                    style="width:30px;height:30px;padding:0;"
                    @click="pickQty=Math.max(1,pickQty-1)">-</button>
                  <span class="fw-bold" style="min-width:24px;text-align:center;">{{ pickQty }}</span>
                  <button class="btn btn-sm btn-outline-secondary rounded-circle"
                    style="width:30px;height:30px;padding:0;"
                    @click="pickQty++">+</button>
                </div>
              </div>
   
              <div class="d-flex gap-2">
                <button class="btn flex-grow-1 fw-bold text-white"
                  style="background:#e91e8c;border-radius:10px;"
                  :disabled="!pickVariant"
                  @click="addToCart">
                  <i class="bi bi-cart-plus me-1"></i>Them vao don
                </button>
                <button class="btn btn-outline-secondary px-4 rounded-3"
                  @click="selectedProduct=null">Huy</button>
              </div>
            </div>
          </div>
        </div>
      </Teleport>
   
      <!-- Receipt modal -->
      <Teleport to="body">
        <div v-if="receipt"
          class="position-fixed d-flex align-items-center justify-content-center"
          style="inset:0;background:rgba(0,0,0,0.6);z-index:4000;padding:16px;">
          <div class="card border-0 shadow-lg" style="width:100%;max-width:380px;border-radius:20px;">
            <div class="card-body p-0">
              <!-- Receipt content -->
              <div id="receipt-content" class="p-4">
                <div class="text-center mb-3">
                  <div class="fw-bold fs-5" style="color:#e91e8c;">CosPlay Shop</div>
                  <div class="small text-muted">Hoa don ban hang tai quay</div>
                  <hr class="my-2">
                  <div class="small text-muted">#{{ receipt.orderNumber }}</div>
                  <div class="small text-muted">{{ formatDateTime(receipt.createdAt) }}</div>
                </div>
   
                <div v-if="receipt.phone || receipt.note" class="mb-2 small">
                  <span v-if="receipt.phone"><strong>KH:</strong> {{ receipt.phone }}</span>
                  <span v-if="receipt.note" class="d-block text-muted">{{ receipt.note }}</span>
                </div>
   
                <hr class="my-2" style="border-style:dashed;">
                <table class="w-100 small mb-2">
                  <tr v-for="item in receipt.items" :key="item.id"
                    class="border-bottom">
                    <td class="py-1">
                      <div>{{ item.productName }}</div>
                      <div class="text-muted" style="font-size:0.72rem;">
                        {{ item.size }}
                        <span v-if="item.itemType==='RENTAL'"> - Thue {{ item.rentalDays }} ngay</span>
                      </div>
                    </td>
                    <td class="py-1 text-end" style="white-space:nowrap;">
                      x{{ item.quantity }}<br>
                      <strong>{{ formatPrice(item.totalPrice) }}</strong>
                    </td>
                  </tr>
                </table>
                <hr class="my-2" style="border-style:dashed;">
   
                <div class="d-flex justify-content-between small mb-1">
                  <span>Tam tinh</span>
                  <span>{{ formatPrice(receipt.subtotalPrice) }}</span>
                </div>
                <div v-if="receipt.finalPrice > receipt.subtotalPrice"
                  class="d-flex justify-content-between small mb-1 text-warning">
                  <span>Tien coc thue</span>
                  <span>{{ formatPrice(receipt.finalPrice - receipt.subtotalPrice) }}</span>
                </div>
                <div class="d-flex justify-content-between fw-bold mb-1">
                  <span>Tong cong</span>
                  <span style="color:#e91e8c;">{{ formatPrice(receipt.finalPrice) }}</span>
                </div>
                <div class="d-flex justify-content-between small text-muted">
                  <span>Thanh toan</span>
                  <span>{{ receipt.paymentMethod==='COD' ? 'Tien mat' : 'Chuyen khoan' }}</span>
                </div>
                <div v-if="lastCashReceived > 0 && receipt.paymentMethod==='COD'"
                  class="d-flex justify-content-between small text-success mt-1">
                  <span>Tien thua</span>
                  <span class="fw-bold">{{ formatPrice(lastCashReceived - receipt.finalPrice) }}</span>
                </div>
   
                <hr class="my-2" style="border-style:dashed;">
                <div class="text-center small text-muted">
                  Cam on quy khach! Hen gap lai.
                </div>
              </div>
   
              <!-- Buttons -->
              <div class="px-4 pb-4 d-flex gap-2">
                <button class="btn flex-grow-1 fw-semibold"
                  style="background:#1a1a2e;color:#fff;border-radius:10px;"
                  @click="printReceipt">
                  <i class="bi bi-printer me-1"></i>In hoa don
                </button>
                <button class="btn btn-outline-secondary px-4 rounded-3"
                  @click="closeReceipt">
                  Xong
                </button>
              </div>
            </div>
          </div>
        </div>
      </Teleport>
    </div>
  </template>
   
  <script setup>
  import { ref, computed, reactive, onMounted } from 'vue'
  import api from '@/api/index'
  import { useToast } from 'vue-toastification'
   
  const toast = useToast()
  const today = new Date().toISOString().split('T')[0]
   
  // ── State ─────────────────────────────────────────────
  const keyword       = ref('')
  const products      = ref([])
  const searching     = ref(false)
  const placing       = ref(false)
  const receipt       = ref(null)
  const lastCashReceived = ref(0)
   
  // ── Multi-tab ─────────────────────────────────────────
  const createTab = () => ({
    id:           Date.now(),
    cartItems:    [],
    customerName:  '',
    customerPhone: '',
    orderNote:    '',
    payMethod:    'COD',
    cashReceived: 0,
  })
   
  const tabs      = ref([createTab()])
  const activeTab = ref(0)
   
  const currentTab = computed(() => tabs.value[activeTab.value] || tabs.value[0])
   
  const addTab = () => {
    tabs.value.push(createTab())
    activeTab.value = tabs.value.length - 1
  }
   
  const closeTab = (i) => {
    if (tabs.value.length === 1) {
      // Reset tab cuoi thay vi xoa
      tabs.value[0] = createTab()
      return
    }
    tabs.value.splice(i, 1)
    activeTab.value = Math.min(activeTab.value, tabs.value.length - 1)
  }
   
  // ── Computed cho tab hien tai ──────────────────────────
  const currentSubtotal = computed(() =>
    currentTab.value.cartItems.reduce((s, i) => {
      if (i.orderType === 'RENTAL')
        return s + i.unitPrice * rentalDays(i) * i.quantity
      return s + i.unitPrice * i.quantity
    }, 0)
  )
   
  const currentDeposit = computed(() =>
    currentTab.value.cartItems
      .filter(i => i.orderType === 'RENTAL')
      .reduce((s, i) => s + (i.depositAmount || 0), 0)
  )
   
  const currentGrandTotal = computed(() => currentSubtotal.value + currentDeposit.value)
   
  const quickCash = computed(() => {
    const g = currentGrandTotal.value
    const set = new Set([g])
    const denoms = [10000, 20000, 50000, 100000, 200000, 500000, 1000000]
    for (const d of denoms) {
      const r = Math.ceil(g / d) * d
      if (r >= g && r <= g * 3) set.add(r)
      if (set.size >= 5) break
    }
    return [...set].sort((a, b) => a - b).slice(0, 5)
  })
   
  // ── Product modal ──────────────────────────────────────
  const selectedProduct = ref(null)
  const pickVariant     = ref(null)
  const pickQty         = ref(1)
  const pickType        = ref('SALE')
  const pickStartDate   = ref(today)
  const pickEndDate     = ref(today)
   
  const pickDays = computed(() => {
    if (!pickStartDate.value || !pickEndDate.value) return 1
    const d = Math.ceil((new Date(pickEndDate.value) - new Date(pickStartDate.value)) / 86400000) + 1
    return Math.max(1, d)
  })
   
  const openProductModal = (p) => {
    selectedProduct.value = p
    pickVariant.value     = null
    pickQty.value         = 1
    pickType.value        = p.productType === 'RENTAL' ? 'RENTAL' : 'SALE'
    pickStartDate.value   = today
    pickEndDate.value     = today
    // Auto chon size neu chi co 1
    if ((p.variants||[]).length === 1) pickVariant.value = p.variants[0]
  }
   
  const addToCart = () => {
    if (!pickVariant.value) { toast.warning('Vui long chon size!'); return }
    const p = selectedProduct.value
    const v = pickVariant.value
    currentTab.value.cartItems.push({
      variantId:       v.id,
      productName:     p.name,
      size:            v.size,
      color:           v.color || '',
      orderType:       pickType.value,
      quantity:        pickQty.value,
      unitPrice:       pickType.value === 'RENTAL'
                         ? (p.rentalPricePerDay || 0)
                         : (v.price || p.salePrice || 0),
      depositAmount:   pickType.value === 'RENTAL'
                         ? (p.depositAmount || 0) * pickQty.value
                         : 0,
      rentalStartDate: pickType.value === 'RENTAL' ? pickStartDate.value : null,
      rentalEndDate:   pickType.value === 'RENTAL' ? pickEndDate.value   : null,
    })
    selectedProduct.value = null
    toast.success('Da them ' + p.name)
  }
   
  // ── Helpers ────────────────────────────────────────────
  const rentalDays = (item) => {
    if (!item.rentalStartDate || !item.rentalEndDate) return 1
    return Math.max(1,
      Math.ceil((new Date(item.rentalEndDate) - new Date(item.rentalStartDate)) / 86400000) + 1)
  }
   
  const itemTotal = (item) => {
    if (item.orderType === 'RENTAL')
      return item.unitPrice * rentalDays(item) * item.quantity + (item.depositAmount || 0)
    return item.unitPrice * item.quantity
  }
   
  const formatPrice = (p) => p != null
    ? new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(p) : ''
   
  const formatDateTime = (d) => d
    ? new Date(d).toLocaleString('vi-VN') : new Date().toLocaleString('vi-VN')
   
  const copyText = (text) => {
    navigator.clipboard.writeText(text).then(() => toast.success('Da copy!'))
  }
   
  // ── Search ─────────────────────────────────────────────
  let searchTimer = null
  const searchDebounce = () => {
    clearTimeout(searchTimer)
    searchTimer = setTimeout(doSearch, 350)
  }
   
  const doSearch = async () => {
    searching.value = true
    try {
      const kw = keyword.value.trim() || ' '
      const res = await api.get('/admin/pos/search-products?keyword=' + encodeURIComponent(kw))
      products.value = res.data.data || []
    } catch (e) {
      products.value = []
    } finally { searching.value = false }
  }
   
  // ── Place order ────────────────────────────────────────
  const placeOrder = async () => {
    const tab = currentTab.value
    if (!tab.cartItems.length) { toast.warning('Gio hang trong!'); return }
    placing.value = true
    try {
      const payload = {
        customerName:  tab.customerName,
        customerPhone: tab.customerPhone,
        paymentMethod: tab.payMethod,
        note:          tab.orderNote,
        items: tab.cartItems.map(i => ({
          variantId:       i.variantId,
          quantity:        i.quantity,
          orderType:       i.orderType,
          rentalStartDate: i.rentalStartDate,
          rentalEndDate:   i.rentalEndDate,
        }))
      }
      const res = await api.post('/admin/pos/orders', payload)
      lastCashReceived.value = tab.cashReceived
      receipt.value = res.data.data
      toast.success('Tao don thanh cong!')
    } catch (e) {
      toast.error(e.response?.data?.message || 'Loi tao don')
    } finally { placing.value = false }
  }
   
  // ── Receipt ────────────────────────────────────────────
  const printReceipt = () => {
    const content = document.getElementById('receipt-content')?.innerHTML
    if (!content) return
    const win = window.open('', '_blank', 'width=380,height=600')
    win.document.write([
      '<html><head><title>Hoa don</title><style>',
      'body{font-family:Arial,sans-serif;font-size:12px;padding:16px;}',
      'table{width:100%;}',
      'hr{border:1px dashed #ccc;}',
      '.text-muted{color:#888;}',
      '.fw-bold,.fw-semibold{font-weight:bold;}',
      '.text-end{text-align:right;}',
      '.text-center{text-align:center;}',
      '.small{font-size:0.85em;}',
      '</style></head><body>',
      content,
      '</body></html>'
    ].join(''))
    win.document.close()
    setTimeout(() => win.print(), 300)
  }
   
  const closeReceipt = () => {
    receipt.value = null
    // Reset tab hien tai, KHONG xoa products
    tabs.value[activeTab.value] = createTab()
    // Giu san pham tren man hinh (khong doSearch lai)
  }
   
  // ── Init ───────────────────────────────────────────────
  onMounted(() => doSearch())
  </script>
   
  <style scoped>
  .rounded-top-3 { border-radius: 12px 12px 0 0 !important; }
  </style>