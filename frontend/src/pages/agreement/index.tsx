import { View, Text, ScrollView } from '@tarojs/components';
import Taro from '@tarojs/taro';
import styles from './index.module.scss';

const SERVICE_SECTIONS = [
  ['一、服务内容', '本小程序用于体育培训中的用户登录、学生资料管理、排课、考勤、课时、体测及相关记录查询。具体功能以实际提供的页面为准。'],
  ['二、账号使用', '用户应提供真实、准确的信息，妥善保管账号，并对账号下的操作负责。不得利用本服务实施违法违规行为或干扰系统正常运行。'],
  ['三、用户权利', '用户可以自主决定是否注册和使用本服务，可以查阅、更正相关资料，并可联系管理员申请注销账号或删除依法允许删除的信息。'],
  ['四、服务变更', '因系统维护、网络故障、政策调整等原因，部分服务可能暂停或调整。我们会在合理范围内保障服务稳定并及时处理问题。'],
  ['五、未成年人保护', '学生信息应由其监护人或经授权的教师维护。监护人应在充分了解功能和信息处理方式后使用本服务。'],
  ['六、协议生效', '用户在未勾选同意前不会被视为接受本协议。用户主动勾选并继续登录或注册后，本协议生效。'],
];

const PRIVACY_SECTIONS = [
  ['一、我们处理的信息', '为提供账号登录、身份识别和培训管理功能，我们可能处理手机号、姓名、微信 OpenID、微信昵称及头像；为管理学生档案、排课、考勤、课时和体测，我们可能处理学生基本资料、监护人联系方式、课程记录、考勤记录、课时余额及流水、体测数据和备注。'],
  ['二、处理目的', '上述信息仅用于身份验证、家长与学生关系管理、教师教学管理、课程安排、签到结课、课时核算、体测记录展示、安全保障和问题处理。'],
  ['三、处理方式', '信息通过加密网络传输并存储在业务服务器中。我们采取访问控制、身份认证和必要的安全措施，限制无关人员访问。'],
  ['四、共享与披露', '除履行法定义务、保障安全或获得用户单独同意外，我们不会向无关第三方出售或披露个人信息。提供基础设施服务的受托方仅在完成服务所需范围内处理信息。'],
  ['五、保存期限', '我们仅在实现服务目的所需期限内保存信息；账号注销或服务终止后，将依据法律要求删除或匿名化处理，依法需要留存的除外。'],
  ['六、用户权利', '用户可通过小程序相关页面查阅或更正资料，也可联系管理员申请复制、删除个人信息、撤回同意或注销账号。撤回同意不影响撤回前基于同意已进行的处理。'],
  ['七、未成年人信息', '学生信息应由监护人提供或授权处理。我们仅为培训管理目的处理未成年人信息，并采取更严格的访问限制。'],
  ['八、联系我们', '如对个人信息处理有疑问，或需要行使个人信息权利，请通过小程序内展示的管理员微信联系方式联系我们。'],
];

export default function AgreementPage() {
  const type = Taro.getCurrentInstance()?.router?.params?.type;
  const isPrivacy = type === 'privacy';
  const title = isPrivacy ? '隐私政策' : '用户服务协议';
  const sections = isPrivacy ? PRIVACY_SECTIONS : SERVICE_SECTIONS;

  return (
    <View className={styles.page}>
      <View className={styles.header}>
        <Text className={styles.title}>{title}</Text>
        <Text className={styles.updatedAt}>更新日期：2026年9月17日</Text>
      </View>
      <ScrollView className={styles.content} scrollY>
        <Text className={styles.intro}>
          请在使用服务前仔细阅读本{isPrivacy ? '政策' : '协议'}。您可以返回上一页，自主选择是否勾选同意。
        </Text>
        {sections.map(([heading, body]) => (
          <View className={styles.section} key={heading}>
            <Text className={styles.heading}>{heading}</Text>
            <Text className={styles.body}>{body}</Text>
          </View>
        ))}
      </ScrollView>
    </View>
  );
}
