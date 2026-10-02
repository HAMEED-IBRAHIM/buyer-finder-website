# 🛋️ DecorLeads — AI-Powered Buyer Discovery

DecorLeads (formerly BuyerFinder) is a modern, high-performance web application designed to help interior decor brands and wholesalers instantly discover and pitch to potential buyers, retailers, and boutique shops across the USA.

Powered by **Google Gemini AI**, DecorLeads eliminates hours of manual research by automatically generating hyper-targeted leads based on simple search queries, and allows users to pitch to them seamlessly with one click.

---

## ✨ Key Features

*   **🧠 AI-Powered Lead Generation:** Integrates with the Google Gemini 1.5 API to instantly identify highly relevant business buyers (names, companies, emails, locations, and websites).
*   **📨 One-Click Outreach:** Built-in email pitching. Users can review a buyer's profile and send a customized, professional pitch directly from the dashboard using Java MailSender (SMTP).
*   **🔐 Secure Authentication:** Enterprise-grade security via **Google OAuth 2.0**. Only verified users can access the platform and run searches.
*   **🎨 Stunning UI/UX:** 
    *   Premium aesthetics featuring subtle glassmorphism and modern gradient avatars.
    *   Dynamic 3D pop-out hover animations on buyer cards.
    *   Custom SVG airplane launch animations for an immersive login experience.
    *   Real-time "bouncing airplane" loading states during email dispatch.
*   **⚡ Optimized Performance:** Implements smart timeout fallbacks and concurrent processing considerations to ensure a rapid, frictionless user experience.

---

## 🛠️ Technology Stack

*   **Backend:** Java 17, Spring Boot 3
*   **Security:** Spring Security, OAuth 2.0 Client
*   **AI Integration:** Google Gemini API (`gemini-1.5-flash`)
*   **Frontend:** HTML5, Vanilla CSS3 (Custom animations, Grid/Flexbox), Vanilla JavaScript
*   **Deployment:** Docker, Render (Cloud Hosting)

---

## 🚀 Getting Started (Local Development)

### Prerequisites
*   Java Development Kit (JDK) 17 or higher
*   Maven
*   A Google Cloud Console account (for OAuth credentials)
*   A Google AI Studio account (for Gemini API Key)

### Environment Variables
To run this project, you will need to set up the following environment variables in your system or your IDE:

```env
# Google OAuth 2.0 Credentials
GOOGLE_CLIENT_ID=your_google_oauth_client_id
GOOGLE_CLIENT_SECRET=your_google_oauth_client_secret

# Gemini AI Credentials
GEMINI_API_KEY=your_gemini_api_key

# Email Sending Credentials (Gmail App Password)
MAIL_USERNAME=your_email@gmail.com
MAIL_PASSWORD=your_16_digit_app_password
```

### Running the Application
1. Clone the repository:
   ```bash
   git clone https://github.com/HAMEED-IBRAHIM/buyer-finder-website.git
   ```
2. Navigate into the directory:
   ```bash
   cd buyer-finder-website
   ```
3. Run the Spring Boot application:
   ```bash
   ./mvnw spring-boot:run
   ```
4. Open your browser and navigate to `http://localhost:8080`

---

## ☁️ Deployment (Render)

This project is fully Dockerized and optimized for deployment on **Render.com**.
1. Create a new "Web Service" on Render.
2. Connect this GitHub repository.
3. Render will automatically detect the `Dockerfile` and build the application.
4. Go to the "Advanced" tab in Render and input your 4 Environment Variables.
5. Deploy!

*(Note: Ensure you add your new Render URL to your Google Cloud Console Authorized Javascript Origins and Redirect URIs!)*
