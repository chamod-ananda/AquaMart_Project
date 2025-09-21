// // Shared Navigation JavaScript for AquaMart

// class AquaMartNavigation {
//   constructor() {
//     this.validPages = [
//       'dashboard.html',
//   'adoptFriend.html', 
//       'myItem.html',
//       'listings.html',
//   'messages.html',
//       'adminDashboard.html',
//       'login.html',
//       'signup.html',
//       'index.html',
//       '404.html',
//       'setting.html',
//       'adminSetting.html',
//       'adminListing.html',
//       'forgetpassword.html',
//       'otpConfirm.html',
//       'resetPassword.html',
//       'adminPayments.html'
//     ];
//     this.init();
//   }

//   init() {
//     try {
//       this.checkPageExists();
//       this.checkAuthentication();
      
//       // Ensure navigation only initializes once DOM is ready
//       if (document.readyState === 'loading') {
//         document.addEventListener('DOMContentLoaded', () => {
//           this.initializeNavigation();
//         });
//       } else {
//         this.initializeNavigation();
//       }
//     } catch (error) {
//       console.error('Error initializing navigation:', error);
//       // Don't retry automatically to prevent endless loops
//       this.handleNavigationError();
//     }
//   }

//   initializeNavigation() {
//     try {
//       this.preventSidebarDuplication();
//       this.setActiveNavItem();
//       this.addNavigationEventListeners();
//       this.addFormGuards();
//       this.setupReloadUXGuards();
//       this.addHoverEffects();
//       this.ensureResponsiveStyles();
//       this.setupSidebarToggle();
      
//       // Don't inject global chat widget on messages page (since it has its own dedicated interface)
//       const currentPage = window.location.pathname.split('/').pop();
//       if (currentPage !== 'messages.html') {
//         this.injectGlobalChat();
//       }
      
//       console.log('AquaMart Navigation initialized successfully');
//     } catch (error) {
//       console.error('Error in navigation initialization:', error);
//       // Retry once after a short delay
//       setTimeout(() => {
//         try {
//           this.initializeNavigation();
//         } catch (retryError) {
//           console.error('Navigation retry failed:', retryError);
//           this.handleNavigationError();
//         }
//       }, 500);
//     }
//   }

//   // Prevent accidental page reloads caused by implicit form submits
//   addFormGuards() {
//     if (this._formGuardsInstalled) return;
//     this._formGuardsInstalled = true;

//     // Prevent default submit on all forms unless opted-in
//     //    Allow pages to opt-in to native submit by adding [data-allow-default] to the form
//     document.addEventListener('submit', (e) => {
//       const form = e.target;
//       if (form && form.matches && form.matches('[data-allow-default]')) {
//         return; // allow default
//       }
//       // Prevent full-page navigation
//       e.preventDefault();
//     }, true); // capture phase to catch early

//     // Prevent Enter key from triggering implicit submit in inputs (except textarea)
//     document.addEventListener('keydown', (e) => {
//       if (e.key !== 'Enter') return;
//       const target = e.target;
//       if (!(target instanceof Element)) return;
//       const form = target.closest('form');
//       if (!form) return;
//       // Allow newlines in textarea
//       if (target.tagName && target.tagName.toLowerCase() === 'textarea') return;

//       // Stop the browser from submitting; instead, dispatch a submit event so page scripts can handle it
//       e.preventDefault();
//       const submitEvent = new Event('submit', { bubbles: true, cancelable: true });
//       form.dispatchEvent(submitEvent);
//     }, true);
//   }

//   // Smart reload UX to avoid disruptive hard reloads
//   setupReloadUXGuards() {
//     if (this._reloadGuardsInstalled) return;
//     this._reloadGuardsInstalled = true;

//     // Internal state
//     this._appNavigating = false;        // set true when our code navigates
//     this._userTypingUntil = 0;          // timestamp until which we treat user as actively typing
//     this._recentInputTimer = null;      // timer to clear recent input flag
//     this._hasRecentInput = false;       // whether user recently interacted with inputs
//     this._reloadSnoozedUntil = 0;       // timestamp until which soft reloads are snoozed

//     // Track user activity in inputs and contenteditable fields
//     const markTyping = () => {
//       this._userTypingUntil = Date.now() + 4000; // 4s grace after last keystroke
//       this._hasRecentInput = true;
//       clearTimeout(this._recentInputTimer);
//       this._recentInputTimer = setTimeout(() => { this._hasRecentInput = false; }, 10000);
//     };

//     document.addEventListener('input', (e) => {
//       const el = e.target;
//       if (!(el instanceof Element)) return;
//       if (el.closest('form') || el.isContentEditable || /input|textarea|select/i.test(el.tagName)) {
//         markTyping();
//       }
//     }, true);
//     document.addEventListener('keydown', (e) => {
//       const el = e.target;
//       if (!(el instanceof Element)) return;
//       if (el.closest('form') || el.isContentEditable || /input|textarea|select/i.test(el.tagName)) {
//         markTyping();
//       }
//     }, true);

//     // Remove intrusive browser confirm; instead we rely on soft reload banner and autosave

//     // Autosave form inputs to sessionStorage and restore on load
//     const STORAGE_KEY = 'bb-autosave';
//     const saveState = () => {
//       try {
//         const data = {};
//         document.querySelectorAll('input, textarea, select').forEach((el) => {
//           if (!(el instanceof Element)) return;
//           const id = el.id || el.name;
//           if (!id) return;
//           if (el.type === 'password') return; // never persist passwords
//           if (el.type === 'checkbox' || el.type === 'radio') {
//             data[id] = { t: el.type, v: el.checked };
//           } else {
//             data[id] = { t: 'value', v: el.value };
//           }
//         });
//         sessionStorage.setItem(STORAGE_KEY, JSON.stringify(data));
//       } catch (_) { /* ignore */ }
//     };
//     const restoreState = () => {
//       try {
//         const raw = sessionStorage.getItem(STORAGE_KEY);
//         if (!raw) return;
//         const data = JSON.parse(raw);
//         Object.keys(data || {}).forEach((key) => {
//           const entry = data[key];
//           const el = document.getElementById(key) || document.querySelector(`[name="${CSS.escape(key)}"]`);
//           if (!el) return;
//           if (entry.t === 'checkbox' || entry.t === 'radio') {
//             el.checked = !!entry.v;
//           } else if (entry.t === 'value') {
//             if (el.type !== 'password') el.value = entry.v ?? '';
//           }
//         });
//       } catch (_) { /* ignore */ }
//     };
//     // Restore soon after DOM ready (initializeNavigation already runs on DOMContentLoaded)
//     setTimeout(restoreState, 0);
//     // Save on changes and periodically
//     document.addEventListener('input', saveState, true);
//     document.addEventListener('change', saveState, true);
//     this._autosaveTimer = setInterval(saveState, 5000);

//   // Public soft-reload API with banner only (no auto reload)
//   // Note: Disabled automatic page reload to prevent disruptive refresh loops.
//   this.scheduleReload = (seconds = 5) => {
//       const now = Date.now();
//       if (now < this._reloadSnoozedUntil) return; // Respect snooze
//       // Create or reuse banner
//       let banner = document.getElementById('bb-soft-reload-banner');
//       if (!banner) {
//         banner = document.createElement('div');
//         banner.id = 'bb-soft-reload-banner';
//         banner.style.cssText = `
//           position: fixed; left: 50%; transform: translateX(-50%);
//           top: 10px; z-index: 2000; background: #2C2A7B; color: #fff;
//           padding: 10px 14px; border-radius: 8px; box-shadow: 0 4px 12px rgba(0,0,0,.2);
//           display: flex; align-items: center; gap: 10px; font-weight: 600;`;
//         banner.innerHTML = `
//           <span id="bb-soft-reload-text">Updating... Reloading in <span id="bb-soft-reload-count">${seconds}</span>s</span>
//           <button id="bb-soft-reload-now" class="btn btn-sm btn-light" style="color:#2C2A7B; font-weight:700;">Reload now</button>
//           <button id="bb-soft-reload-snooze" class="btn btn-sm btn-outline-light">Snooze</button>`;
//         document.body.appendChild(banner);
//         banner.querySelector('#bb-soft-reload-now').addEventListener('click', () => {
//           // Manual reload only when user clicks
//           this._appNavigating = true;
//           location.reload();
//         });
//         banner.querySelector('#bb-soft-reload-snooze').addEventListener('click', () => {
//           // Snooze for 2 minutes by default
//           this._reloadSnoozedUntil = Date.now() + 2 * 60 * 1000;
//           banner.remove();
//         });
//       }

//       // Show banner only; do not auto reload. Countdown disabled.
//       const countEl = banner.querySelector('#bb-soft-reload-count');
//       if (countEl) countEl.textContent = String(seconds);
//       clearInterval(this._reloadTimer);
//     };

//     // Expose a way to bypass prompt for next navigation if needed
//     this.allowNextUnload = () => { this._appNavigating = true; setTimeout(() => this._appNavigating = false, 3000); };
//   }

//   ensureResponsiveStyles(){
//     // Ensure global responsive stylesheet is present
//     if(!document.querySelector('link[href$="/assets/css/responsive.css"], link[href*="assets/css/responsive.css"]')){
//       const link = document.createElement('link');
//       link.rel = 'stylesheet';
//       // Compute relative path from typical pages dir
//       const isInPages = /\/pages\//.test(window.location.pathname.replace(/\\/g,'/'));
//       link.href = isInPages ? '../assets/css/responsive.css' : 'assets/css/responsive.css';
//       document.head.appendChild(link);
//     }
//   }

//   setupSidebarToggle(){
//     const sidebar = document.querySelector('.sidebar');
//     if(!sidebar) return;

//     // Only add on small screens
//     const addToggle = () => {
//       const existingBtn = document.getElementById('bb-sidebar-toggle');
//       const existingOverlay = document.getElementById('bb-sidebar-overlay');
//       const isSmall = window.matchMedia('(max-width: 992px)').matches;

//       if(isSmall){
//         // Ensure overlay exists first so handlers can reference it
//         let overlay = existingOverlay;
//         if(!overlay){
//           overlay = document.createElement('div');
//           overlay.id = 'bb-sidebar-overlay';
//           overlay.className = 'sidebar-overlay';
//           overlay.addEventListener('click', () => {
//             sidebar.classList.remove('show');
//             overlay.classList.remove('show');
//             const btn = document.getElementById('bb-sidebar-toggle');
//             btn?.setAttribute('aria-expanded','false');
//           });
//           document.body.appendChild(overlay);
//         }

//         // Toggle button
//         if(!existingBtn){
//           const btn = document.createElement('button');
//           btn.id = 'bb-sidebar-toggle';
//           btn.className = 'bb-sidebar-toggle';
//           btn.setAttribute('aria-label','Open Menu');
//           btn.setAttribute('aria-expanded','false');
//           btn.innerHTML = '<i class="fas fa-bars"></i>';
//           btn.addEventListener('click', () => {
//             const willOpen = !sidebar.classList.contains('show');
//             if(willOpen){
//               sidebar.classList.add('show');
//               overlay.classList.add('show');
//               btn.setAttribute('aria-expanded','true');
//             } else {
//               sidebar.classList.remove('show');
//               overlay.classList.remove('show');
//               btn.setAttribute('aria-expanded','false');
//             }
//           });
//           document.body.appendChild(btn);
//         }

//         // Close sidebar with ESC key
//         if(!this._escHandler){
//           this._escHandler = (e) => {
//             if(e.key === 'Escape'){
//               const ov = document.getElementById('bb-sidebar-overlay');
//               if(ov && ov.classList.contains('show')){
//                 sidebar.classList.remove('show');
//                 ov.classList.remove('show');
//                 const btn = document.getElementById('bb-sidebar-toggle');
//                 btn?.setAttribute('aria-expanded','false');
//               }
//             }
//           };
//           document.addEventListener('keydown', this._escHandler);
//         }

//         // Close sidebar when navigating
//         document.querySelectorAll('.sidebar .nav-link').forEach(a => {
//           a.addEventListener('click', () => {
//             sidebar.classList.remove('show');
//             const ov = document.getElementById('bb-sidebar-overlay');
//             ov && ov.classList.remove('show');
//             const btn = document.getElementById('bb-sidebar-toggle');
//             btn?.setAttribute('aria-expanded','false');
//           });
//         });
//       } else {
//         // Cleanup on larger screens
//         sidebar.classList.remove('show');
//         existingBtn?.remove();
//         existingOverlay?.remove();
//         if(this._escHandler){
//           document.removeEventListener('keydown', this._escHandler);
//           this._escHandler = null;
//         }
//       }
//     };

//     addToggle();
//     window.addEventListener('resize', () => {
//       // debounce a bit
//       clearTimeout(this._resizeTimer);
//       this._resizeTimer = setTimeout(addToggle, 150);
//     });
//   }

//   injectGlobalChat(){
//     // add chat css & script once
//     if(!document.getElementById('bb-chat-css')) {
//       const link = document.createElement('link');
//       link.id='bb-chat-css';
//       link.rel='stylesheet';
//       link.href='../assets/css/chat.css';
//       document.head.appendChild(link);
//     }
//     if(!document.getElementById('bb-chat-js')) {
//       const script = document.createElement('script');
//       script.id='bb-chat-js';
//       script.src='../assets/js/chat.js';
//       document.body.appendChild(script);
//     }
//   }

//   handleNavigationError() {
//     // Create a minimal fallback navigation if main nav fails
//     const fallbackNav = document.createElement('div');
//     fallbackNav.innerHTML = `
//       <div class="alert alert-warning position-fixed top-0 start-0 w-100" style="z-index: 9999;" role="alert">
//         <i class="fas fa-exclamation-triangle"></i> 
//         Navigation temporarily unavailable. <a href="dashboard.html" class="alert-link">Return to Dashboard</a>
//         <button type="button" class="btn-close float-end" onclick="this.parentElement.remove()"></button>
//       </div>
//     `;
//     document.body.insertBefore(fallbackNav, document.body.firstChild);
//   }

//   checkPageExists() {
//     const currentPage = window.location.pathname.split('/').pop();
//     if (currentPage && !this.validPages.includes(currentPage) && currentPage !== '') {
//       // Redirect to 404 page for invalid pages
//       window.location.href = '404.html';
//     }
//   }

//   preventSidebarDuplication() {
//     // Remove any duplicate sidebars that might exist
//     const sidebars = document.querySelectorAll('.sidebar');
//     if (sidebars.length > 1) {
//       // Keep only the first sidebar
//       for (let i = 1; i < sidebars.length; i++) {
//         sidebars[i].remove();
//       }
//     }
//   }

//   checkAuthentication() {
//     const token = localStorage.getItem('accessToken');
//     const currentPage = window.location.pathname.split('/').pop();
//     const publicPages = ['login.html', 'signup.html', 'index.html', '404.html', 'forgetpassword.html', 'forgetpasswordNew.html', 'otpConfirm.html', 'otpConfirmNew.html', 'resetPassword.html'];
    
//     if (!token && !publicPages.includes(currentPage)) {
//       this.showNotification('Please log in first', 'error');
//       // Use a more gentle approach instead of immediate redirect
//       setTimeout(() => {
//         if (!localStorage.getItem('accessToken')) { // Double check before redirect
//           window.location.href = 'login.html';
//         }
//       }, 2000);
//     }
//   }

//   setActiveNavItem() {
//     const currentPage = window.location.pathname.split('/').pop();
//     const sidebarNavLinks = document.querySelectorAll('.sidebar .nav-link');
    
//     sidebarNavLinks.forEach(link => {
//       link.classList.remove('active');
//       const href = link.getAttribute('href');
      
//       // Match current page with navigation links
//       if (href === currentPage || 
//           (currentPage === 'dashboard.html' && href === 'dashboard.html') ||
//           (currentPage === 'adoptFriend.html' && href === 'adoptFriend.html') ||
//           (currentPage === 'myItem.html' && href === 'myItem.html') ||
//           (currentPage === 'listings.html' && href === 'listings.html') ||
//           (currentPage === 'messages.html' && href === 'messages.html') ||
//           (currentPage === 'setting.html' && href === 'setting.html') ||
//           (currentPage === 'adminSetting.html' && href === 'adminSetting.html') ||
//           (currentPage === 'adminListing.html' && href === 'adminListing.html') ||
//           (currentPage === 'messages.html' && href === 'messages.html') ||
//           (currentPage === 'login.html' && href === 'login.html') ||
//           (currentPage === 'signup.html' && href === 'signup.html') ||
//           (currentPage === 'index.html' && href === 'index.html') ||
//           (currentPage === '404.html' && href === '404.html') ||
//           (currentPage === 'forgetpassword.html' && href === 'forgetpassword.html') ||
//           (currentPage === 'otpConfirm.html' && href === 'otpConfirm.html') ||
//           (currentPage === 'resetPassword.html' && href === 'resetPassword.html') ||
//           (currentPage === 'adminPayments.html' && href === 'adminPayments.html') ||
//           (currentPage === 'adminDashboard.html' && href === 'adminDashboard.html')) {
//         link.classList.add('active');
//       }
//     });
//   }

//   addNavigationEventListeners() {
//     const sidebarNavLinks = document.querySelectorAll('.sidebar .nav-link');
    
//     sidebarNavLinks.forEach(link => {
//       // Remove any existing event listeners to prevent duplicates
//       link.removeEventListener('click', this.handleNavClick);
      
//       link.addEventListener('click', (e) => {
//         const href = link.getAttribute('href');
        
//         // Handle logout separately
//         if (link.textContent.includes('Logout')) {
//           e.preventDefault();
//           this.logout();
//           return;
//         }
        
//         // Don't prevent default for valid pages - let normal navigation work
//         if (href && href !== '#' && this.validPages.includes(href)) {
//           // Remove active class from all links
//           sidebarNavLinks.forEach(l => l.classList.remove('active'));
//           // Add active class to clicked link
//           link.classList.add('active');
          
//           // Add loading state for valid navigation
//           this.showNavigationLoading(link);
          
//           // Track navigation for analytics
//           console.log('Navigation to:', href);
//           return; // Allow default navigation
//         }
        
//         // Prevent navigation for invalid pages
//         if (href && href !== '#' && !this.validPages.includes(href)) {
//           e.preventDefault();
//           this.showNotification('Page not found', 'error');
//           // Don't auto-redirect to prevent navigation loops
//           console.warn('Invalid page navigation blocked:', href);
//           return;
//         }
//       });
//     });
//   }

//   addHoverEffects() {
//     const sidebarNavLinks = document.querySelectorAll('.sidebar .nav-link');
    
//     sidebarNavLinks.forEach(link => {
//       link.addEventListener('mouseenter', function() {
//         if (!this.classList.contains('active')) {
//           this.style.transform = 'translateX(5px)';
//         }
//       });
      
//       link.addEventListener('mouseleave', function() {
//         if (!this.classList.contains('active')) {
//           this.style.transform = 'translateX(0)';
//         }
//       });
//     });
//   }

//   showNavigationLoading(link) {
//     const originalText = link.innerHTML;
//     link.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Loading...';
    
//     setTimeout(() => {
//       link.innerHTML = originalText;
//     }, 800);
//   }

//     logout() {
//     Swal.fire({
//       title: 'Are you sure?',
//       text: "You will be logged out from the system!",
//       icon: 'warning',
//       showCancelButton: true,
//       confirmButtonColor: '#3085d6',
//       cancelButtonColor: '#d33',
//       confirmButtonText: 'Yes, logout'
//     }).then((result) => {
//       if (result.isConfirmed) {
//         // Clear all stored data
//         localStorage.removeItem('accessToken');
//         localStorage.removeItem('userId');
//         localStorage.removeItem('userRole');
//         localStorage.removeItem('userEmail');

//         // Show success alert
//         Swal.fire({
//           title: 'Logged Out!',
//           text: 'You have been logged out successfully. �',
//           icon: 'success',
//           timer: 1500,
//           showConfirmButton: false
//         }).then(() => {
//           // Redirect after alert closes
//           window.location.href = 'login.html';
//         });
//       }
//     });
//   }


//   showNotification(message, type = 'success') {
//     // Create notification if it doesn't exist
//     let notification = document.getElementById('navigation-notification');
//     if (!notification) {
//       notification = document.createElement('div');
//       notification.id = 'navigation-notification';
//       notification.style.cssText = `
//         position: fixed;
//         top: 20px;
//         right: 20px;
//         padding: 15px 20px;
//         border-radius: 10px;
//         color: white;
//         opacity: 0;
//         transform: translateY(-20px);
//         transition: all 0.3s ease;
//         z-index: 1001;
//         font-weight: 600;
//         box-shadow: 0 4px 12px rgba(0,0,0,0.2);
//         max-width: 300px;
//       `;
//       document.body.appendChild(notification);
//     }
    
//     notification.textContent = message;
//     notification.className = `notification show ${type}`;
//     notification.style.backgroundColor = type === 'success' ? '#1cc88a' : '#e74a3b';
//     notification.style.opacity = '1';
//     notification.style.transform = 'translateY(0)';
    
//     setTimeout(() => {
//       notification.style.opacity = '0';
//       notification.style.transform = 'translateY(-20px)';
//     }, 4000);
//   }

//   // Navigation helper functions
//   navigateToAdopt() {
//     this.navigateToPage('adoptFriend.html');
//   }

  

//   navigateToDashboard() {
//     this.navigateToPage('dashboard.html');
//   }

//   navigateToMyItems() {
//     this.navigateToPage('myItem.html');
//   }

//   navigateToSettings() {
//     this.navigateToPage('setting.html');
//   }

//   navigateToAdminSettings() {
//     this.navigateToPage('adminSetting.html');
//   }

//   navigateToAdminPayments() {
//     this.navigateToPage('adminPayments.html');
//   }

//   navigateToPage(page) {
//     if (this.validPages.includes(page)) {
//       // Mark as app-driven navigation to avoid beforeunload prompt
//       this._appNavigating = true;
//       window.location.href = page;
//     } else {
//       this.showNotification('Page not found', 'error');
//       setTimeout(() => {
//         this._appNavigating = true;
//         window.location.href = '404.html';
//       }, 1000);
//     }
//   }

//   // Error handling for missing pages
//   handlePageError() {
//     window.addEventListener('error', (e) => {
//       console.error('Page error:', e);
//       this.showNotification('Something went wrong', 'error');
//     });

//     // Handle 404 errors
//     window.addEventListener('unhandledrejection', (e) => {
//       console.error('Unhandled promise rejection:', e);
//       if (e.reason && e.reason.status === 404) {
//         window.location.href = '404.html';
//       }
//     });
//   }

//   // Method to refresh navigation (useful for dynamic page updates)
//   refreshNavigation() {
//     this.setActiveNavItem();
//     this.addNavigationEventListeners();
//     this.addHoverEffects();
//     this.preventSidebarDuplication();
//   }

//   // Method to manually set active page
//   setActivePage(pageName) {
//     const sidebarNavLinks = document.querySelectorAll('.sidebar .nav-link');
//     sidebarNavLinks.forEach(link => {
//       link.classList.remove('active');
//       const href = link.getAttribute('href');
//       if (href === pageName) {
//         link.classList.add('active');
//       }
//     });
//   }
// }

// // Initialize navigation when DOM is loaded
// document.addEventListener('DOMContentLoaded', function() {
//   window.barkBuddyNav = new AquaMartNavigation();
// });

// // Export for use in other scripts
// if (typeof module !== 'undefined' && module.exports) {
//   module.exports = AquaMartNavigation;
// }

class AquaMartNavigation {
  constructor() {
    this.validPages = [
      'dashboard.html',
  'adoptFriend.html', 
      'myItem.html',
      'listings.html',
  'messages.html',
      'adminDashboard.html',
      'login.html',
      'signup.html',
      'index.html',
      '404.html',
      'setting.html',
      'adminSetting.html',
      'adminListing.html',
      'forgetpassword.html',
      'otpConfirm.html',
      'resetPassword.html',
      'adminPayments.html'
    ];
    this.init();
  }

  init() {
    try {
      this.checkPageExists();
      this.checkAuthentication();
      
      // Ensure navigation only initializes once DOM is ready
      if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', () => {
          this.initializeNavigation();
        });
      } else {
        this.initializeNavigation();
      }
    } catch (error) {
      console.error('Error initializing navigation:', error);
      // Don't retry automatically to prevent endless loops
      this.handleNavigationError();
    }
  }

  initializeNavigation() {
    try {
      this.preventSidebarDuplication();
      this.setActiveNavItem();
      this.addNavigationEventListeners();
      this.addFormGuards();
      this.setupReloadUXGuards();
      this.addHoverEffects();
      this.ensureResponsiveStyles();
      this.setupSidebarToggle();
      
      // Don't inject global chat widget on messages page (since it has its own dedicated interface)
      const currentPage = window.location.pathname.split('/').pop();
      if (currentPage !== 'messages.html') {
        this.injectGlobalChat();
      }
      
      console.log('AquaMart Navigation initialized successfully');
    } catch (error) {
      console.error('Error in navigation initialization:', error);
      // Retry once after a short delay
      setTimeout(() => {
        try {
          this.initializeNavigation();
        } catch (retryError) {
          console.error('Navigation retry failed:', retryError);
          this.handleNavigationError();
        }
      }, 500);
    }
  }

  // Prevent accidental page reloads caused by implicit form submits
  addFormGuards() {
    if (this._formGuardsInstalled) return;
    this._formGuardsInstalled = true;

    // Prevent default submit on all forms unless opted-in
    //    Allow pages to opt-in to native submit by adding [data-allow-default] to the form
    document.addEventListener('submit', (e) => {
      const form = e.target;
      if (form && form.matches && form.matches('[data-allow-default]')) {
        return; // allow default
      }
      // Prevent full-page navigation
      e.preventDefault();
    }, true); // capture phase to catch early

    // Prevent Enter key from triggering implicit submit in inputs (except textarea)
    document.addEventListener('keydown', (e) => {
      if (e.key !== 'Enter') return;
      const target = e.target;
      if (!(target instanceof Element)) return;
      const form = target.closest('form');
      if (!form) return;
      // Allow newlines in textarea
      if (target.tagName && target.tagName.toLowerCase() === 'textarea') return;

      // Stop the browser from submitting; instead, dispatch a submit event so page scripts can handle it
      e.preventDefault();
      const submitEvent = new Event('submit', { bubbles: true, cancelable: true });
      form.dispatchEvent(submitEvent);
    }, true);
  }

  // Smart reload UX to avoid disruptive hard reloads
  setupReloadUXGuards() {
    if (this._reloadGuardsInstalled) return;
    this._reloadGuardsInstalled = true;

    // Internal state
    this._appNavigating = false;        // set true when our code navigates
    this._userTypingUntil = 0;          // timestamp until which we treat user as actively typing
    this._recentInputTimer = null;      // timer to clear recent input flag
    this._hasRecentInput = false;       // whether user recently interacted with inputs
    this._reloadSnoozedUntil = 0;       // timestamp until which soft reloads are snoozed

    // Track user activity in inputs and contenteditable fields
    const markTyping = () => {
      this._userTypingUntil = Date.now() + 4000; // 4s grace after last keystroke
      this._hasRecentInput = true;
      clearTimeout(this._recentInputTimer);
      this._recentInputTimer = setTimeout(() => { this._hasRecentInput = false; }, 10000);
    };

    document.addEventListener('input', (e) => {
      const el = e.target;
      if (!(el instanceof Element)) return;
      if (el.closest('form') || el.isContentEditable || /input|textarea|select/i.test(el.tagName)) {
        markTyping();
      }
    }, true);
    document.addEventListener('keydown', (e) => {
      const el = e.target;
      if (!(el instanceof Element)) return;
      if (el.closest('form') || el.isContentEditable || /input|textarea|select/i.test(el.tagName)) {
        markTyping();
      }
    }, true);

    // Remove intrusive browser confirm; instead we rely on soft reload banner and autosave

    // Autosave form inputs to sessionStorage and restore on load
    const STORAGE_KEY = 'bb-autosave';
    const saveState = () => {
      try {
        const data = {};
        document.querySelectorAll('input, textarea, select').forEach((el) => {
          if (!(el instanceof Element)) return;
          const id = el.id || el.name;
          if (!id) return;
          if (el.type === 'password') return; // never persist passwords
          if (el.type === 'checkbox' || el.type === 'radio') {
            data[id] = { t: el.type, v: el.checked };
          } else {
            data[id] = { t: 'value', v: el.value };
          }
        });
        sessionStorage.setItem(STORAGE_KEY, JSON.stringify(data));
      } catch (_) { /* ignore */ }
    };
    const restoreState = () => {
      try {
        const raw = sessionStorage.getItem(STORAGE_KEY);
        if (!raw) return;
        const data = JSON.parse(raw);
        Object.keys(data || {}).forEach((key) => {
          const entry = data[key];
          const el = document.getElementById(key) || document.querySelector([name="${CSS.escape(key)}"]);
          if (!el) return;
          if (entry.t === 'checkbox' || entry.t === 'radio') {
            el.checked = !!entry.v;
          } else if (entry.t === 'value') {
            if (el.type !== 'password') el.value = entry.v ?? '';
          }
        });
      } catch (_) { /* ignore */ }
    };
    // Restore soon after DOM ready (initializeNavigation already runs on DOMContentLoaded)
    setTimeout(restoreState, 0);
    // Save on changes and periodically
    document.addEventListener('input', saveState, true);
    document.addEventListener('change', saveState, true);
    this._autosaveTimer = setInterval(saveState, 5000);

  // Public soft-reload API with banner only (no auto reload)
  // Note: Disabled automatic page reload to prevent disruptive refresh loops.
  this.scheduleReload = (seconds = 5) => {
      const now = Date.now();
      if (now < this._reloadSnoozedUntil) return; // Respect snooze
      // Create or reuse banner
      let banner = document.getElementById('bb-soft-reload-banner');
      if (!banner) {
        banner = document.createElement('div');
        banner.id = 'bb-soft-reload-banner';
        banner.style.cssText = `
          position: fixed; left: 50%; transform: translateX(-50%);
          top: 10px; z-index: 2000; background: #2C2A7B; color: #fff;
          padding: 10px 14px; border-radius: 8px; box-shadow: 0 4px 12px rgba(0,0,0,.2);
          display: flex; align-items: center; gap: 10px; font-weight: 600;`;
        banner.innerHTML = `
          <span id="bb-soft-reload-text">Updating... Reloading in <span id="bb-soft-reload-count">${seconds}</span>s</span>
          <button id="bb-soft-reload-now" class="btn btn-sm btn-light" style="color:#2C2A7B; font-weight:700;">Reload now</button>
          <button id="bb-soft-reload-snooze" class="btn btn-sm btn-outline-light">Snooze</button>`;
        document.body.appendChild(banner);
        banner.querySelector('#bb-soft-reload-now').addEventListener('click', () => {
          // Manual reload only when user clicks
          this._appNavigating = true;
          location.reload();
        });
        banner.querySelector('#bb-soft-reload-snooze').addEventListener('click', () => {
          // Snooze for 2 minutes by default
          this._reloadSnoozedUntil = Date.now() + 2 * 60 * 1000;
          banner.remove();
        });
      }

      // Show banner only; do not auto reload. Countdown disabled.
      const countEl = banner.querySelector('#bb-soft-reload-count');
      if (countEl) countEl.textContent = String(seconds);
      clearInterval(this._reloadTimer);
    };

    // Expose a way to bypass prompt for next navigation if needed
    this.allowNextUnload = () => { this._appNavigating = true; setTimeout(() => this._appNavigating = false, 3000); };
  }

  ensureResponsiveStyles(){
    // Ensure global responsive stylesheet is present
    if(!document.querySelector('link[href$="/assets/css/responsive.css"], link[href*="assets/css/responsive.css"]')){
      const link = document.createElement('link');
      link.rel = 'stylesheet';
      // Compute relative path from typical pages dir
      const isInPages = /\/pages\//.test(window.location.pathname.replace(/\\/g,'/'));
      link.href = isInPages ? '../assets/css/responsive.css' : 'assets/css/responsive.css';
      document.head.appendChild(link);
    }
  }

  setupSidebarToggle(){
    const sidebar = document.querySelector('.sidebar');
    if(!sidebar) return;

    // Only add on small screens
    const addToggle = () => {
      const existingBtn = document.getElementById('bb-sidebar-toggle');
      const existingOverlay = document.getElementById('bb-sidebar-overlay');
      const isSmall = window.matchMedia('(max-width: 992px)').matches;

      if(isSmall){
        // Ensure overlay exists first so handlers can reference it
        let overlay = existingOverlay;
        if(!overlay){
          overlay = document.createElement('div');
          overlay.id = 'bb-sidebar-overlay';
          overlay.className = 'sidebar-overlay';
          overlay.addEventListener('click', () => {
            sidebar.classList.remove('show');
            overlay.classList.remove('show');
            const btn = document.getElementById('bb-sidebar-toggle');
            btn?.setAttribute('aria-expanded','false');
          });
          document.body.appendChild(overlay);
        }

        // Toggle button
        if(!existingBtn){
          const btn = document.createElement('button');
          btn.id = 'bb-sidebar-toggle';
          btn.className = 'bb-sidebar-toggle';
          btn.setAttribute('aria-label','Open Menu');
          btn.setAttribute('aria-expanded','false');
          btn.innerHTML = '<i class="fas fa-bars"></i>';
          btn.addEventListener('click', () => {
            const willOpen = !sidebar.classList.contains('show');
            if(willOpen){
              sidebar.classList.add('show');
              overlay.classList.add('show');
              btn.setAttribute('aria-expanded','true');
            } else {
              sidebar.classList.remove('show');
              overlay.classList.remove('show');
              btn.setAttribute('aria-expanded','false');
            }
          });
          document.body.appendChild(btn);
        }

        // Close sidebar with ESC key
        if(!this._escHandler){
          this._escHandler = (e) => {
            if(e.key === 'Escape'){
              const ov = document.getElementById('bb-sidebar-overlay');
              if(ov && ov.classList.contains('show')){
                sidebar.classList.remove('show');
                ov.classList.remove('show');
                const btn = document.getElementById('bb-sidebar-toggle');
                btn?.setAttribute('aria-expanded','false');
              }
            }
          };
          document.addEventListener('keydown', this._escHandler);
        }

        // Close sidebar when navigating
        document.querySelectorAll('.sidebar .nav-link').forEach(a => {
          a.addEventListener('click', () => {
            sidebar.classList.remove('show');
            const ov = document.getElementById('bb-sidebar-overlay');
            ov && ov.classList.remove('show');
            const btn = document.getElementById('bb-sidebar-toggle');
            btn?.setAttribute('aria-expanded','false');
          });
        });
      } else {
        // Cleanup on larger screens
        sidebar.classList.remove('show');
        existingBtn?.remove();
        existingOverlay?.remove();
        if(this._escHandler){
          document.removeEventListener('keydown', this._escHandler);
          this._escHandler = null;
        }
      }
    };

    addToggle();
    window.addEventListener('resize', () => {
      // debounce a bit
      clearTimeout(this._resizeTimer);
      this._resizeTimer = setTimeout(addToggle, 150);
    });
  }

  injectGlobalChat(){
    // add chat css & script once
    if(!document.getElementById('bb-chat-css')) {
      const link = document.createElement('link');
      link.id='bb-chat-css';
      link.rel='stylesheet';
      link.href='../assets/css/chat.css';
      document.head.appendChild(link);
    }
    if(!document.getElementById('bb-chat-js')) {
      const script = document.createElement('script');
      script.id='bb-chat-js';
      script.src='../assets/js/chat.js';
      document.body.appendChild(script);
    }
  }

  handleNavigationError() {
    // Create a minimal fallback navigation if main nav fails
    const fallbackNav = document.createElement('div');
    fallbackNav.innerHTML = `
      <div class="alert alert-warning position-fixed top-0 start-0 w-100" style="z-index: 9999;" role="alert">
        <i class="fas fa-exclamation-triangle"></i> 
        Navigation temporarily unavailable. <a href="dashboard.html" class="alert-link">Return to Dashboard</a>
        <button type="button" class="btn-close float-end" onclick="this.parentElement.remove()"></button>
      </div>
    `;
    document.body.insertBefore(fallbackNav, document.body.firstChild);
  }

  checkPageExists() {
    const currentPage = window.location.pathname.split('/').pop();
    if (currentPage && !this.validPages.includes(currentPage) && currentPage !== '') {
      // Redirect to 404 page for invalid pages
      window.location.href = '404.html';
    }
  }

  preventSidebarDuplication() {
    // Remove any duplicate sidebars that might exist
    const sidebars = document.querySelectorAll('.sidebar');
    if (sidebars.length > 1) {
      // Keep only the first sidebar
      for (let i = 1; i < sidebars.length; i++) {
        sidebars[i].remove();
      }
    }
  }

  checkAuthentication() {
    const token = localStorage.getItem('accessToken');
    const currentPage = window.location.pathname.split('/').pop();
    const publicPages = ['login.html', 'signup.html', 'index.html', '404.html', 'forgetpassword.html', 'forgetpasswordNew.html', 'otpConfirm.html', 'otpConfirmNew.html', 'resetPassword.html'];
    
    if (!token && !publicPages.includes(currentPage)) {
      this.showNotification('Please log in first', 'error');
      // Use a more gentle approach instead of immediate redirect
      setTimeout(() => {
        if (!localStorage.getItem('accessToken')) { // Double check before redirect
          window.location.href = 'login.html';
        }
      }, 2000);
    }
  }

  setActiveNavItem() {
    const currentPage = window.location.pathname.split('/').pop();
    const sidebarNavLinks = document.querySelectorAll('.sidebar .nav-link');
    
    sidebarNavLinks.forEach(link => {
      link.classList.remove('active');
      const href = link.getAttribute('href');
      
      // Match current page with navigation links
      if (href === currentPage || 
          (currentPage === 'dashboard.html' && href === 'dashboard.html') ||
          (currentPage === 'adoptFriend.html' && href === 'adoptFriend.html') ||
          (currentPage === 'myItem.html' && href === 'myItem.html') ||
          (currentPage === 'listings.html' && href === 'listings.html') ||
          (currentPage === 'messages.html' && href === 'messages.html') ||
          (currentPage === 'setting.html' && href === 'setting.html') ||
          (currentPage === 'adminSetting.html' && href === 'adminSetting.html') ||
          (currentPage === 'adminListing.html' && href === 'adminListing.html') ||
          (currentPage === 'messages.html' && href === 'messages.html') ||
          (currentPage === 'login.html' && href === 'login.html') ||
          (currentPage === 'signup.html' && href === 'signup.html') ||
          (currentPage === 'index.html' && href === 'index.html') ||
          (currentPage === '404.html' && href === '404.html') ||
          (currentPage === 'forgetpassword.html' && href === 'forgetpassword.html') ||
          (currentPage === 'otpConfirm.html' && href === 'otpConfirm.html') ||
          (currentPage === 'resetPassword.html' && href === 'resetPassword.html') ||
          (currentPage === 'adminPayments.html' && href === 'adminPayments.html') ||
          (currentPage === 'adminDashboard.html' && href === 'adminDashboard.html')) {
        link.classList.add('active');
      }
    });
  }

  addNavigationEventListeners() {
    const sidebarNavLinks = document.querySelectorAll('.sidebar .nav-link');
    
    sidebarNavLinks.forEach(link => {
      // Remove any existing event listeners to prevent duplicates
      link.removeEventListener('click', this.handleNavClick);
      
      link.addEventListener('click', (e) => {
        const href = link.getAttribute('href');
        
        // Handle logout separately
        if (link.textContent.includes('Logout')) {
          e.preventDefault();
          this.logout();
          return;
        }
        
        // Don't prevent default for valid pages - let normal navigation work
        if (href && href !== '#' && this.validPages.includes(href)) {
          // Remove active class from all links
          sidebarNavLinks.forEach(l => l.classList.remove('active'));
          // Add active class to clicked link
          link.classList.add('active');
          
          // Add loading state for valid navigation
          this.showNavigationLoading(link);
          
          // Track navigation for analytics
          console.log('Navigation to:', href);
          return; // Allow default navigation
        }
        
        // Prevent navigation for invalid pages
        if (href && href !== '#' && !this.validPages.includes(href)) {
          e.preventDefault();
          this.showNotification('Page not found', 'error');
          // Don't auto-redirect to prevent navigation loops
          console.warn('Invalid page navigation blocked:', href);
          return;
        }
      });
    });
  }

  addHoverEffects() {
    const sidebarNavLinks = document.querySelectorAll('.sidebar .nav-link');
    
    sidebarNavLinks.forEach(link => {
      link.addEventListener('mouseenter', function() {
        if (!this.classList.contains('active')) {
          this.style.transform = 'translateX(5px)';
        }
      });
      
      link.addEventListener('mouseleave', function() {
        if (!this.classList.contains('active')) {
          this.style.transform = 'translateX(0)';
        }
      });
    });
  }

  showNavigationLoading(link) {
    const originalText = link.innerHTML;
    link.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Loading...';
    
    setTimeout(() => {
      link.innerHTML = originalText;
    }, 800);
  }

    logout() {
    Swal.fire({
      title: 'Are you sure?',
      text: "You will be logged out from the system!",
      icon: 'warning',
      showCancelButton: true,
      confirmButtonColor: '#3085d6',
      cancelButtonColor: '#d33',
      confirmButtonText: 'Yes, logout'
    }).then((result) => {
      if (result.isConfirmed) {
        // Clear all stored data
        localStorage.removeItem('accessToken');
        localStorage.removeItem('userId');
        localStorage.removeItem('userRole');
        localStorage.removeItem('userEmail');

        // Show success alert
        Swal.fire({
          title: 'Logged Out!',
          text: 'You have been logged out successfully.',
          icon: 'success',
          timer: 1500,
          showConfirmButton: false
        }).then(() => {
          // Redirect after alert closes
          window.location.href = 'login.html';
        });
      }
    });
  }


  showNotification(message, type = 'success') {
    // Create notification if it doesn't exist
    let notification = document.getElementById('navigation-notification');
    if (!notification) {
      notification = document.createElement('div');
      notification.id = 'navigation-notification';
      notification.style.cssText = `
        position: fixed;
        top: 20px;
        right: 20px;
        padding: 15px 20px;
        border-radius: 10px;
        color: white;
        opacity: 0;
        transform: translateY(-20px);
        transition: all 0.3s ease;
        z-index: 1001;
        font-weight: 600;
        box-shadow: 0 4px 12px rgba(0,0,0,0.2);
        max-width: 300px;
      `;
      document.body.appendChild(notification);
    }
    
    notification.textContent = message;
  notification.className = `notification show ${type}`;
    notification.style.backgroundColor = type === 'success' ? '#1cc88a' : '#e74a3b';
    notification.style.opacity = '1';
    notification.style.transform = 'translateY(0)';
    
    setTimeout(() => {
      notification.style.opacity = '0';
      notification.style.transform = 'translateY(-20px)';
    }, 4000);
  }

  // Navigation helper functions
  navigateToAdopt() {
    this.navigateToPage('adoptFriend.html');
  }

  

  navigateToDashboard() {
    this.navigateToPage('dashboard.html');
  }

  navigateToMyItems() {
    this.navigateToPage('myItem.html');
  }

  navigateToSettings() {
    this.navigateToPage('setting.html');
  }

  navigateToAdminSettings() {
    this.navigateToPage('adminSetting.html');
  }

  navigateToAdminPayments() {
    this.navigateToPage('adminPayments.html');
  }

  navigateToPage(page) {
    if (this.validPages.includes(page)) {
      // Mark as app-driven navigation to avoid beforeunload prompt
      this._appNavigating = true;
      window.location.href = page;
    } else {
      this.showNotification('Page not found', 'error');
      setTimeout(() => {
        this._appNavigating = true;
        window.location.href = '404.html';
      }, 1000);
    }
  }

  // Error handling for missing pages
  handlePageError() {
    window.addEventListener('error', (e) => {
      console.error('Page error:', e);
      this.showNotification('Something went wrong', 'error');
    });

    // Handle 404 errors
    window.addEventListener('unhandledrejection', (e) => {
      console.error('Unhandled promise rejection:', e);
      if (e.reason && e.reason.status === 404) {
        window.location.href = '404.html';
      }
    });
  }

  // Method to refresh navigation (useful for dynamic page updates)
  refreshNavigation() {
    this.setActiveNavItem();
    this.addNavigationEventListeners();
    this.addHoverEffects();
    this.preventSidebarDuplication();
  }

  // Method to manually set active page
  setActivePage(pageName) {
    const sidebarNavLinks = document.querySelectorAll('.sidebar .nav-link');
    sidebarNavLinks.forEach(link => {
      link.classList.remove('active');
      const href = link.getAttribute('href');
      if (href === pageName) {
        link.classList.add('active');
      }
    });
  }
}

// Initialize navigation when DOM is loaded
document.addEventListener('DOMContentLoaded', function() {
  window.barkBuddyNav = new AquaMartNavigation();
});

// Export for use in other scripts
if (typeof module !== 'undefined' && module.exports) {
  module.exports = AquaMartNavigation;
}
