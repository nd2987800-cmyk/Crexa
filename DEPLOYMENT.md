# 🚀 CREXA - Deployment & GitHub Push Guide

A complete guide on pushing your project to GitHub and deploying it automatically using GitHub Actions (GitHub Pages).

---

## 1. Quick One-Time GitHub Push Steps

Follow these steps on your local machine to push this project to GitHub:

### Step 1: Create a Repository on GitHub
1. Go to [https://github.com/new](https://github.com/new).
2. Enter Repository Name: `crexa-app` (or any name you prefer).
3. Set visibility to **Public** (recommended for free GitHub Pages hosting).
4. Click **Create repository** (do not check "Initialize with README").

### Step 2: Initialize Git & Push from Local Folder
Open your terminal/command prompt inside your project directory and run:

```bash
# 1. Initialize git
git init

# 2. Add all files
git add .

# 3. Commit files
git commit -m "feat: complete CREXA app with automated deployment"

# 4. Rename default branch to main
git branch -M main

# 5. Connect to your GitHub repository (replace USERNAME and REPO_NAME)
git remote add origin https://github.com/<YOUR_GITHUB_USERNAME>/<YOUR_REPO_NAME>.git

# 6. Push code to GitHub
git push -u origin main
```

---

## 2. Enable GitHub Pages Automated Deployment

Once your code is pushed, enable GitHub Pages:

1. Go to your repository on GitHub.
2. Click on **Settings** (top right tab).
3. In the left sidebar, click **Pages** (under Code and automation).
4. Under **Build and deployment > Source**, select:
   * **GitHub Actions**
5. Go to the **Actions** tab in your repository to see the `Deploy to GitHub Pages` workflow running automatically!
6. Once completed, your live site URL will be provided (e.g. `https://<YOUR_GITHUB_USERNAME>.github.io/<YOUR_REPO_NAME>/`).

---

## 3. Automated Updates
Every time you push new code to the `main` branch, GitHub Actions will automatically trigger and redeploy your live website instantly without any manual work!
