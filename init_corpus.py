# 初始化并存储所提供的语料库
import json, re

def sep_chinese_char(content):                  # 中文内容按行切分
    pattern = re.compile(r'[^\u4e00-\u9fa5]+')
    return re.sub(pattern, '\n', content)

basedir = ".\\materials\\gbk_news\\"
suffix = ".txt"
g = open(".\\data\\corpus.txt", mode="w", encoding= "utf8")       # 覆盖写入模式
#
for i in range(1, 10):
    file = basedir + str(i).zfill(2) + suffix
    f = open(file, mode= "r", encoding="gbk")
    lines = f.readlines()
    for line in lines:
        news = json.loads(line.strip())
        words = sep_chinese_char(news["html"]).strip()
        g.write(words)

f.close()
g.close()

