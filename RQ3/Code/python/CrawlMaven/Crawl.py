import time

from bs4 import BeautifulSoup
from bs4.element import Tag
import requests
from selenium.webdriver import Chrome
from selenium.webdriver.common.by import By
from PageObjects import *
import json

base_url = 'https://maven.google.com/web/index.html'

data = {}

def load_data():
    global data
    with open('newData.json') as data_json:
        data = json.load(data_json)


if __name__ == '__main__':
    load_data()
    driver = Chrome()
    driver.implicitly_wait(10)

    page = BasePage('https://maven.google.com/web/index.html', driver=driver, timeout=30)
    page.open()
    tree_selector = page.find_element(By.XPATH, '/html/body/div[1]/div/aside/div')
    time.sleep(3)
    children = tree_selector.find_elements(By.XPATH, './*')
    for i, child in enumerate(children):
        if 'android.support' in child.text or 'androidx' in child.text:
            if 'androidx.core' not in child.text:
                continue
            if child.text in data.keys():
                continue
            time.sleep(1)
            temp_child = page.find_element(By.XPATH, f'/html/body/div[1]/div/aside/div/div[{i + 1}]')
            print(child.text)
            data.setdefault(child.text, {})
            child.click()
            time.sleep(0.3)

            div = page.find_element(By.XPATH, '/html/body/div/div/main/div')
            artifacts = div.find_elements(By.XPATH, './*')
            artifacts_total = [i.text.lstrip("folder_open ") for i in artifacts[2:]]
            print(artifacts_total)
            length = len(artifacts)

            temp_group = data[child.text]
            time.sleep(0.3)
            for j in range(3, length + 1):
                page.click(By.XPATH, f'/html/body/div/div/main/div/div[{j}]')
                time.sleep(0.3)
                artifact = artifacts_total[j - 3]
                temp_group.setdefault(artifact, {})
                temp_artifact = temp_group[artifact]

                versions = page.find_elements(By.XPATH, f'/html/body/div/div/main/div/div')
                versions_total = [i.text for i in versions[2:]]
                print(len(versions_total))
                for k in range(3, len(versions) + 1):
                    page.click(By.XPATH, f'/html/body/div/div/main/div/div[{k}]')
                    version = versions_total[k - 3]
                    temp_artifact.setdefault(version, {})
                    temp_version = temp_artifact[version]

                    try:
                        WebDriverWait(driver, 30, 0.1).until(
                            EC.text_to_be_present_in_element((By.XPATH, '/html/body/div/div/main/div'), '.'))
                    except Exception:
                        driver.back()
                        continue
                    element = driver.find_element(By.XPATH, '/html/body/div/div/main/div')

                    # 处理每个版本里的详细数据
                    trs = element.find_element(By.TAG_NAME, 'tbody').find_elements(By.XPATH, './*')
                    for tr in trs:
                        tds = tr.find_elements(By.XPATH, './*')
                        span = tds[1].find_elements(By.XPATH, './*')
                        total = tds[1].find_elements(By.XPATH, './/*')
                        if 'a' in [a_.tag_name for a_ in total]:
                            a_set = {}
                            for a_ in total:
                                if a_.tag_name == 'a':
                                    a_set.setdefault(a_.text, a_.get_attribute("href"))
                            temp_version.setdefault(tds[0].text, a_set)
                        else:
                            temp_version.setdefault(tds[0].text, tds[1].text)

                    driver.back()
                driver.back()
            json_data = json.dumps(data)
            with open('newData.json', 'w') as file:
                file.write(json_data)
            time.sleep(5)

    driver.close()
