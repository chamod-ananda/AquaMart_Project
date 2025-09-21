// Enhanced WhatsApp-like Chat Widget
(function(){
  if(window.AquaMartChatInitialized) return; // prevent duplicate
  window.AquaMartChatInitialized = true;

  const token = localStorage.getItem('accessToken');
  if(!token) return; // not logged in

  // Enhanced Modal HTML
  const modalHtml = `
  <div class="modal fade chat-modal" id="globalChatModal" tabindex="-1">
    <div class="modal-dialog modal-dialog-centered modal-lg">
      <div class="modal-content border-0">
        <div class="chat-container">
          <div class="chat-conv-list">
            <div class="chat-conv-header">
              <span><i class="fas fa-comments me-2"></i>Chats</span>
              <button class="btn btn-sm btn-light" id="refreshConvsBtn" title="Refresh">
                <i class='fas fa-rotate'></i>
              </button>
            </div>
            <div id="chatConversations" style="flex:1; overflow-y:auto;"></div>
          </div>
          <div class="chat-panel">
            <div class="chat-panel-header">
              <div>
                <div id="chatActiveTitle" class="fish">Select a conversation</div>
                <small id="chatStatus" class="text-light opacity-75">Click on a chat to start messaging</small>
              </div>
              <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"></button>
            </div>
            <div id="chatMessages" class="chat-messages"></div>
            <div class="chat-input-bar" id="chatInputBar" style="display:none;">
              <label class="btn btn-success" title="Attach image">
                <i class="fas fa-image"></i>
                <input type="file" id="chatImageInput" accept="image/*" style="display:none;" />
              </label>
              <input type="text" id="chatInput" placeholder="Type a message..." maxlength="500" />
              <button id="chatSendBtn" title="Send message">
                <i class='fas fa-paper-plane'></i>
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>`;
  document.body.insertAdjacentHTML('beforeend', modalHtml);

  const chatModal = new bootstrap.Modal(document.getElementById('globalChatModal'));
  const convsDiv = document.getElementById('chatConversations');
  const msgsDiv = document.getElementById('chatMessages');
  const chatInputBar = document.getElementById('chatInputBar');
  const chatActiveTitle = document.getElementById('chatActiveTitle');
  const chatStatus = document.getElementById('chatStatus');
  const chatInput = document.getElementById('chatInput');
  let current = { itemId:null, otherUserId:null, itemName:'', otherUsername:'' };
  let lastMessageCount = 0;

  function truncate(str,len){ return str&&str.length>len?str.slice(0,len-1)+'…':str||''; }
  
  function showTypingIndicator() {
    const typing = document.createElement('div');
    typing.className = 'typing-indicator';
    typing.innerHTML = '<div class="typing-dot"></div><div class="typing-dot"></div><div class="typing-dot"></div>';
    typing.id = 'typingIndicator';
    msgsDiv.appendChild(typing);
    msgsDiv.scrollTop = msgsDiv.scrollHeight;
  }
  
  function hideTypingIndicator() {
    const typing = document.getElementById('typingIndicator');
    if(typing) typing.remove();
  }

  async function loadConversations(){
    convsDiv.innerHTML = '<div class="p-3 text-center text-muted small"><div class="spinner-border spinner-border-sm me-2"></div>Loading conversations...</div>';
    try {
      const res = await fetch('http://localhost:8080/api/user-chat/conversations',{ headers:{ 'Authorization':'Bearer '+token }});
      if(!res.ok) throw new Error('Failed to load conversations');
      const data = await res.json();
      
      if(!Array.isArray(data) || data.length===0){
        convsDiv.innerHTML = `
          <div class="p-4 text-center text-muted">
            <i class="fas fa-comments fa-3x mb-3 opacity-50"></i>
            <p class="mb-2">No conversations yet</p>
            <small>Start chatting with fish owners from adoption pages</small>
          </div>`;
        return;
      }
      
      convsDiv.innerHTML='';
      data.forEach((c, index)=>{
        const item = document.createElement('div');
        item.className = 'chat-conv-item';
        if(current.itemId === c.itemId && current.otherUserId === c.otherUserId) item.classList.add('active');
        
        const time = new Date(c.lastTimestamp).toLocaleTimeString([], {hour:'2-digit', minute:'2-digit'});
        const isToday = new Date(c.lastTimestamp).toDateString() === new Date().toDateString();
        const displayTime = isToday ? time : new Date(c.lastTimestamp).toLocaleDateString();
        
        item.innerHTML = `
          <div class="title">
            <i class="fas fa-fish text-warning me-1"></i> 
            ${truncate(c.itemName,16)}
          </div>
          <div class="preview">${truncate(c.lastMessage,35)}</div>
          <div class="time">${displayTime}</div>`;

        item.onclick = ()=> openConversation(c.itemId, c.otherUserId, c.itemName, c.otherUsername);
        convsDiv.appendChild(item);
        
        // Add subtle animation delay
        setTimeout(()=> item.style.transform = 'translateX(0)', index * 50);
      });
    } catch(e){
      convsDiv.innerHTML = `<div class='p-3 text-danger small'><i class="fas fa-exclamation-triangle me-1"></i>${e.message}</div>`;
    }
  }

  function formatDay(ts){ 
    const d = new Date(ts); 
    const today = new Date();
    const yesterday = new Date(today);
    yesterday.setDate(yesterday.getDate() - 1);
    
    if(d.toDateString() === today.toDateString()) return 'Today';
    if(d.toDateString() === yesterday.toDateString()) return 'Yesterday';
    return d.toLocaleDateString();
  }

  async function loadMessages(){
    if(!current.itemId) return;
    hideTypingIndicator();
    
    try {
      const res = await fetch(`http://localhost:8080/api/user-chat/conversation/${current.itemId}/${current.otherUserId}`, {
        headers:{ 'Authorization':'Bearer '+token }
      });
      if(!res.ok) throw new Error('Failed to load messages');
      
      const data = await res.json();
      const myId = Number(localStorage.getItem('userId'));
      msgsDiv.innerHTML='';
      
      if(!Array.isArray(data) || data.length===0){
        msgsDiv.innerHTML = `
          <div class="chat-empty">
            <i class="fas fa-comments"></i>
            <p class="mb-0">No messages yet</p>
            <small class="text-muted">Say hello to start the conversation! 👋</small>
          </div>`;
        return;
      }
      
      let lastDay = '';
      data.forEach((m, index)=>{
        const day = formatDay(m.timestamp);
        if(day !== lastDay){
          const divDay = document.createElement('div');
          divDay.className='chat-day-divider';
          divDay.textContent = day;
          msgsDiv.appendChild(divDay);
          lastDay = day;
        }
        
        const b = document.createElement('div');
        b.className = 'bubble ' + (m.senderId===myId? 'me':'other');
        const time = new Date(m.timestamp).toLocaleTimeString([], {hour:'2-digit', minute:'2-digit'});
        
        let content = '';
        if(m.content) {
          content += m.content.replace(/</g,'&lt;').replace(/\n/g,'<br>');
        }
        if(m.imageUrl) {
          content += `<div><img src="${m.imageUrl}" alt="Image" style="max-width:200px; border-radius:8px; margin-top:4px; cursor:pointer;" onclick="window.open('${m.imageUrl}', '_blank')"/></div>`;
        }
        
        const status = m.senderId === myId ? '<span class="message-status"><i class="fas fa-check"></i></span>' : '';
        b.innerHTML = `${content}<div class="time">${time} ${status}</div>`;
        
        msgsDiv.appendChild(b);
        
        // Add entrance animation
        setTimeout(()=> b.style.opacity = '1', index * 20);
      });
      
      msgsDiv.scrollTop = msgsDiv.scrollHeight;
      updateChatStatus('Active now');
    } catch(e){
      msgsDiv.innerHTML = `<div class='p-3 text-danger small'><i class="fas fa-exclamation-triangle"></i> ${e.message}</div>`;
      updateChatStatus('Failed to load');
    }
  }

  function updateChatStatus(status) {
    chatStatus.textContent = status;
  }

  async function sendMessage(){
    const content = chatInput.value.trim();
    const imageInput = document.getElementById('chatImageInput');
    
    if(!content && (!imageInput.files || imageInput.files.length===0)) {
      chatInput.focus();
      return;
    }
    if(!current.itemId) return;
    
    // Prevent self-chat
    const myId = Number(localStorage.getItem('userId'));
    if(myId === current.otherUserId){
      const toast = document.createElement('div');
      toast.className = 'toast align-items-center text-white bg-info border-0 position-fixed';
      toast.style.cssText = 'top:20px; right:20px; z-index:9999;';
      toast.innerHTML = `
        <div class="d-flex">
          <div class="toast-body"><i class="fas fa-info-circle me-2"></i>� This is your own item! You cannot chat with yourself.</div>
          <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast"></button>
        </div>`;
      document.body.appendChild(toast);
      const bsToast = new bootstrap.Toast(toast);
      bsToast.show();
      setTimeout(() => toast.remove(), 4000);
      return;
    }
    
    // Disable inputs during send
    const sendBtn = document.getElementById('chatSendBtn');
    chatInput.disabled = true;
    sendBtn.disabled = true;
    sendBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i>';
    
    // Show typing indicator for better UX
    if(content) showTypingIndicator();
    
    try {
      if(imageInput.files && imageInput.files.length > 0) {
        const fd = new FormData();
        fd.append('itemId', String(current.itemId));
        fd.append('receiverId', String(current.otherUserId));
        if(content) fd.append('content', content);
        fd.append('image', imageInput.files[0]);
        
        const res = await fetch('http://localhost:8080/api/user-chat/send-image', { 
          method:'POST', 
          headers:{ 'Authorization':'Bearer '+token }, 
          body: fd 
        });
        
        if(!res.ok) {
          const errorData = await res.json().catch(() => ({}));
          throw new Error(errorData.message || 'Failed to send image');
        }
        imageInput.value='';
      } else {
        const res = await fetch('http://localhost:8080/api/user-chat/send', { 
          method:'POST', 
          headers:{ 'Content-Type':'application/json','Authorization':'Bearer '+token }, 
          body: JSON.stringify({ itemId: current.itemId, receiverId: current.otherUserId, content }) 
        });
        
        if(!res.ok) {
          const errorData = await res.json().catch(() => ({}));
          throw new Error(errorData.message || 'Failed to send message');
        }
      }
      
      chatInput.value='';
      hideTypingIndicator();
      await loadMessages();
      loadConversations(); // Update conversation list
      updateChatStatus('Message sent');
      
    } catch(e){
      hideTypingIndicator();
      updateChatStatus('Failed to send');
      console.error('Send error:', e);
      
      // Show user-friendly error
      const errorMsg = document.createElement('div');
      errorMsg.className = 'alert alert-danger alert-dismissible fade show position-fixed';
      errorMsg.style.cssText = 'top:20px; right:20px; z-index:9999; max-width:300px;';
      errorMsg.innerHTML = `
        <small><i class="fas fa-exclamation-triangle me-1"></i>${e.message}</small>
        <button type="button" class="btn-close btn-close" data-bs-dismiss="alert"></button>`;
      document.body.appendChild(errorMsg);
      setTimeout(() => errorMsg.remove(), 5000);
      
    } finally {
      chatInput.disabled = false;
      sendBtn.disabled = false;
      sendBtn.innerHTML = '<i class="fas fa-paper-plane"></i>';
      chatInput.focus();
    }
  }

  function openConversation(itemId, otherUserId, itemName, otherUsername){
    const myId = Number(localStorage.getItem('userId'));
    if(myId === otherUserId){
      // Show friendly message instead of error
      const toast = document.createElement('div');
      toast.className = 'toast align-items-center text-white bg-info border-0 position-fixed';
      toast.style.cssText = 'top:20px; right:20px; z-index:9999;';
      toast.innerHTML = `
        <div class="d-flex">
          <div class="toast-body"><i class="fas fa-info-circle me-2"></i>� This is your own item! You cannot chat with yourself.</div>
          <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast"></button>
        </div>`;
      document.body.appendChild(toast);
      const bsToast = new bootstrap.Toast(toast);
      bsToast.show();
      setTimeout(() => toast.remove(), 4000);
      return;
    }

    current = { itemId, otherUserId, itemName, otherUsername };
    chatActiveTitle.textContent = `${itemName}`;
    updateChatStatus(`Chat with ${otherUsername}`);
    chatInputBar.style.display='flex';
    
    // Update active conversation
    document.querySelectorAll('.chat-conv-item').forEach(i => i.classList.remove('active'));
    
    loadMessages();
    chatInput.focus();
    
    // Highlight active conversation after a delay
    setTimeout(()=>{
      document.querySelectorAll('.chat-conv-item').forEach(i=>{
        if(i.querySelector('.title').textContent.includes(itemName)) {
          i.classList.add('active');
        }
      });
    }, 200);
  }
  
  // Global function for fish cards
  window.openConversation = openConversation;

  // Enhanced function for fish cards
  window.openChat = function(itemId, ownerId, itemName){
    const myId = Number(localStorage.getItem('userId'));
    if(myId === ownerId){
      // Show friendly message instead of error
      const toast = document.createElement('div');
      toast.className = 'toast align-items-center text-white bg-info border-0 position-fixed';
      toast.style.cssText = 'top:20px; right:20px; z-index:9999;';
      toast.innerHTML = `
        <div class="d-flex">
          <div class="toast-body"><i class="fas fa-info-circle me-2"></i>This is your own item! �</div>
          <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast"></button>
        </div>`;
      document.body.appendChild(toast);
      const bsToast = new bootstrap.Toast(toast);
      bsToast.show();
      setTimeout(() => toast.remove(), 4000);
      return;
    }
    
    chatModal.show();
    loadConversations().then(()=>{
      openConversation(itemId, ownerId, itemName, 'Owner');
    });
  };

  // Event Listeners
  document.getElementById('refreshConvsBtn').addEventListener('click', loadConversations);
  document.getElementById('chatSendBtn').addEventListener('click', sendMessage);
  
  // Enhanced input handling
  chatInput.addEventListener('keydown', e => { 
    if(e.key === 'Enter' && !e.shiftKey){ 
      e.preventDefault(); 
      sendMessage(); 
    }
  });
  
  // Auto-resize input (future enhancement)
  chatInput.addEventListener('input', function() {
    this.style.height = 'auto';
    this.style.height = Math.min(this.scrollHeight, 120) + 'px';
  });

  // Improved polling with smarter updates
  // Optional polling (disabled by default to avoid auto-refresh flicker)
  let pollInterval = null;
  function startChatPolling() {
    if (pollInterval) clearInterval(pollInterval);
    pollInterval = setInterval(()=>{ 
      const modalEl = document.getElementById('globalChatModal'); 
      if(modalEl && modalEl.classList.contains('show')) {
        if(current.itemId) {
          loadMessages();
        }
        loadConversations();
      }
    }, 6000);
  }
  // Wire start on explicit user action only (open chat)
  document.addEventListener('shown.bs.modal', (e) => {
    if (e.target && e.target.id === 'globalChatModal') {
      startChatPolling();
    }
  });

  // Cleanup on page unload
  window.addEventListener('beforeunload', () => {
    if(pollInterval) clearInterval(pollInterval);
  });

})();