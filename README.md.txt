# AI-assisted Quantitative Histologic Analysis of Non-metastatic Regional Lymph Nodes in MSI Colorectal Cancer

This repository contains the code and scripts used in the paper:

> **AI-assisted quantitative histologic analysis of non-metastatic regional lymph nodes predicts prognosis in MSI colorectal cancer**

## 🧠 Overview

This project uses a combination of QuPath Groovy scripts and Python code to analyze lymph nodes (LNs) in colorectal cancer whole slide images (WSIs).  
The workflow includes annotation adjustments, morphometric feature extraction, and data integration for prognostic analysis.

## 📂 Repository Structure

- `.groovy`: Groovy scripts used in **QuPath** to:
  - Adjust annotations for lymph nodes (LNs), germinal centers (GCs), and tumor regions
  - Extract morphometric features such as area, diameter, and perimeter

- `.ipynb`: Python Jupyter Notebooks for:
  - Merging extracted data by case ID
  - Further data processing, cleaning, and statistical analysis

## 🛠️ Tools & Dependencies

### 🧬 QuPath
- [QuPath v0.4.3+](https://qupath.github.io)
- Used for region detection, segmentation, and annotation scripting

### 🐍 Python (for post-processing)
- Python 3.8+
- Install dependencies:
  ```bash
  pip install -r requirements.txt
