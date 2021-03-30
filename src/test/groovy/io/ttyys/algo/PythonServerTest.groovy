package io.ttyys.algo

import algo.text.Message
import io.ttyys.algo.springboot.EnableAlgoSupport
import io.ttyys.algo.springboot.SpringBootAutoConfiguration
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest(classes = SpringBootAutoConfiguration)
@EnableAlgoSupport
class SimilarityAlgoTest {

    /**
     * 文件所在文件夹绝对路径
     */
    String folderPath = '/Volumes/works/tmp/text'

    /**
     * 停用词表绝对路径-详情参看jieba停用词表
     */
    String stopWordFile = '/Volumes/works/tmp/text/.similarity/stop_word'

    /**
     * 用户字典-详情参看jieba用户字典
     */
    String userDict = '/Volumes/works/tmp/text/.similarity/user_dict'

    /**
     * 余弦相似度结果存储文件地址绝对路径
     */
    String cosResultFile = '/Volumes/works/tmp/text/.similarity/cos_result'

    /**
     * SimHash相似度结果存储文件绝对路径
     */
    String simResultFile = '/Volumes/works/tmp/text/.similarity/sim_result'

    /**
     * 制作语料库
     */
    @Test
    void makeCorpus() {
        String resp = AlgorithmFactory.ALGORITHM.invoker().send(
                Message.newBuilder()
                        .setFolderPath(this.folderPath)
                        .setStopWordFile(this.stopWordFile)
                        .setUserDict(this.userDict)
                        .setCosResultFile(this.cosResultFile)
                        .setSimResultFile(this.simResultFile)
                        .build())
        println resp
    }


    /**
     * 比较一个文件与整个文档库的相似度(其与文档库中所有文档的相似度的平均结果),该结果基于平均值,可能无应用场景
     */
    @Test
    String compareToLib() {
        // 此方法可能无实际使用场景 伪码如下:
        // 1. 计算余弦结果
        // 1.1 获取余弦结果文件,因余弦结果存储时是存在重复结果,故只取第一列的文件名判断与传入文件名是否相似
        // 1.2 相似的一组全部放入集合内
        // 1.3 该集合百分比求平均数 即为该文件与语料库的余弦相似度
        // 2. 计算SimHash结果
        // 2.1 获取simhash结果文件,该文件结果去重过,所以,要把他在两列是这个文件名的全部捞到
        // 2.2 将所获得的结果集合进行求平均数,得到simhash相似度
    }

    /**
     * 和文件比较
     */
    @Test
    void compareToFile() {
        // 此方法用于获取两个文件基于整体语料库范围的相似度比较
        // 1. 计算两个文件余弦结果
        // 1.1 得到余弦结果文件 首先将第一个文件作为列检索其结果,按照二维结构,以文件名为key,其值列表为value,建议得到后存储至redis,方便后续使用
        // 1.2 按照key检索其内部的值,找到所匹配的另一文件的条目,获取其相似度,构造SimilarityResult对象
        // 2. 计算两个文件的SimHash结果
        // 2.1 获取simhash结果文件,该文件结果去重过,所以只要该文件在条目里出现,即需要进行收集,统一收集至集合内部,以文件名为key,其值列表为value,建议得到后存储至redis,方便后续使用
        // 2.2 以文件名获取其相似度值
    }
}

class SimilarityResult {

    /**
     * 需比对文件名
     */
    String fileName

    /**
     * 余弦结果
     */
    Double cosResult

    /**
     * SimHash结果
     */
    Double simResult

    /**
     * 和谁比较的,和Lib比较则为CORPUS_LIB,其余为文件名
     */
    String compareTo
}
