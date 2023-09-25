import time
import json
from selenium import webdriver
from selenium.webdriver.chrome.options import Options
from selenium.webdriver.common.by import By
from selenium.webdriver.common.action_chains import ActionChains
from selenium.webdriver.remote.webelement import WebElement
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.support.wait import WebDriverWait
from selenium.webdriver.common.keys import Keys
from selenium.webdriver.common.desired_capabilities import DesiredCapabilities


class BasePage(object):
    def __init__(self, base_url, driver=None, timeout=10):
        self.base_url = base_url
        self.driver = webdriver.Chrome() if driver is None else driver
        self.driver.implicitly_wait(timeout)

    def _open(self):
        self.driver.get(self.base_url)

    def open(self):
        self._open()

    def open_url(self, _url):
        self.driver.get(_url)

    def find_element(self, *loc) -> WebElement:
        return WebDriverWait(self.driver, 10, 0.5).until(EC.presence_of_element_located(loc))

    def find_elements(self, *loc):
        WebDriverWait(self.driver, 10, 0.5).until(EC.presence_of_element_located(loc))
        return self.driver.find_elements(*loc)

    def click(self, *loc) -> WebElement:
        element = WebDriverWait(self.driver, 10, 0.5).until(EC.element_to_be_clickable(loc))
        try:
            element.click()
        except Exception:
            self.driver.execute_script('arguments[0].click()', element)  # If it cannot click, use JS to perform click
        return element

    def double_click(self, *loc) -> WebElement:
        element = WebDriverWait(self.driver, 10, 0.5).until(EC.element_to_be_clickable(loc))
        ActionChains(self.driver).double_click(element).perform()
        return element

    def tri_click(self, *loc) -> WebElement:
        element = WebDriverWait(self.driver, 10, 0.5).until(EC.element_to_be_clickable(loc))
        ActionChains(self.driver).click(element).double_click(element).perform()
        return element

    def get_cookie(self, name):
        return self.driver.get_cookie(name)

    def get_cookies(self):
        return self.driver.get_cookies()

    def add_cookie(self, cookie: dict):
        cookie_dict = {
            'name': cookie.get('name'),
            'value': cookie.get('value')
        }
        self.driver.add_cookie(cookie_dict)

    def refresh(self):
        self.driver.refresh()

    def maximize(self):
        self.driver.maximize_window()

    def execute_script(self, script):
        return self.driver.execute_script(script)

    def quit(self):
        self.driver.quit()


class LoginPage(BasePage):
    username_loc = (By.NAME, "username")
    password_loc = (By.NAME, "password")
    login_btn = (By.CLASS_NAME, "el-button")

    def type_username(self, username):
        u = self.double_click(*self.username_loc)
        u.send_keys(username)

    def type_password(self, password):
        p = self.double_click(*self.password_loc)
        p.send_keys(password)

    def type_login(self):
        self.click(*self.login_btn)


class LiteMallPage(BasePage):

    # 添加优惠券的locs
    add_loc = (By.XPATH, '//*[@id="app"]/div/div[2]/section/div/div[1]/button[2]')
    specified_classification = (By.XPATH, '//*[@id="app"]/div/div[2]/section/div/div[4]/div/div[2]/form/div[12]/div/div/label[2]')
    specified_good = (By.XPATH, '//*[@id="app"]/div/div[2]/section/div/div[4]/div/div[2]/form/div[12]/div/div/label[3]')
    good_name = (By.XPATH, '//*[@id="app"]/div/div[2]/section/div/div[4]/div/div[2]/form/div[14]/div/div[1]/div/input')
    good_kind = (By.XPATH, '//*[@id="app"]/div/div[2]/section/div/div[4]/div/div[2]/form/div[13]/div/div[1]/div/input')
    ul = (By.XPATH, '/html/body/div[5]/div[1]/div[1]/ul')
    add_good = (By.XPATH, '//*[@id="app"]/div/div[2]/section/div/div[4]/div/div[2]/form/div[14]/div/button')
    discount_coupon_name = (By.XPATH, '//*[@id="app"]/div/div[2]/section/div/div[4]/div/div[2]/form/div[1]/div/div[1]/input')
    confirm = (By.XPATH, '//*[@id="app"]/div/div[2]/section/div/div[4]/div/div[3]/div/button[2]')
    dialog = (By.XPATH, '//*[@id="app"]/div/div[2]/section/div/div[4]')

    # 查找优惠券的locs
    title_loc = (By.XPATH, '//*[@id="app"]/div/div[2]/section/div/div[1]/div[1]/input')
    type_loc = (By.XPATH, '//*[@id="app"]/div/div[2]/section/div/div[1]/div[2]/div[1]/input')
    status_loc = (By.XPATH, '//*[@id="app"]/div/div[2]/section/div/div[1]/div[3]/div[1]/input')
    search_loc = (By.XPATH, '//*[@id="app"]/div/div[2]/section/div/div[1]/button[1]')
    tbody_loc = (By.XPATH, '//*[@id="app"]/div/div[2]/section/div/div[2]/div[3]/table/tbody')

    # 测试删除的locs
    delete_loc = (By.XPATH, '//*[@id="app"]/div/div[2]/section/div/div[2]/div[3]/table/tbody/tr/td[12]/div/button[3]')

    def type_add_discount_coupon(self):
        self.click(*self.add_loc)

    def type_specified_classification(self):
        self.click(*self.specified_good)
        gn = self.click(*self.good_name)
        time.sleep(2)
        gn.send_keys('测试开发实战教程')
        time.sleep(2)
        gn.send_keys(Keys.DOWN)
        time.sleep(0.5)
        gn.send_keys(Keys.ENTER)
        self.click(*self.add_good)

    def type_specified_good(self):
        self.click(*self.specified_good)
        gn = self.click(*self.good_name)
        time.sleep(2)
        gn.send_keys('测试开发实战教程')
        time.sleep(2)
        gn.send_keys(Keys.DOWN)
        time.sleep(0.5)
        gn.send_keys(Keys.ENTER)
        self.click(*self.add_good)

    def type_data(self, data):
        name = self.click(*self.discount_coupon_name)
        name.send_keys(data)

    def type_datas(self, datas):
        name = self.click(*self.discount_coupon_name)
        for i, data in enumerate(datas):
            name.send_keys(data)
            time.sleep(0.2)
            name.send_keys(Keys.TAB)

    def type_confirm(self):
        self.click(*self.confirm)
        time.sleep(1)
        return not self.find_element(*self.dialog).is_displayed()

    def type_title(self, data):
        title = self.tri_click(*self.title_loc)
        title.send_keys(data)

    def type_search(self):
        self.click(*self.search_loc)
        time.sleep(1)

    def type_delete(self):
        self.click(*self.delete_loc)

    def get_tbody(self):
        tbody = self.find_element(*self.tbody_loc)
        return tbody



