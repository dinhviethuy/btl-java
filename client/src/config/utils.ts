import { blue, green, grey, orange, red } from "@ant-design/colors";

export const SKILLS_LIST = [
  // 🎮 Game Development
  { label: "Unity", value: "UNITY" },
  { label: "Unreal Engine", value: "UNREAL ENGINE" },
  { label: "Cocos2d", value: "COCOS2D" },
  { label: "Phaser", value: "PHASER" },
  { label: "Godot", value: "GODOT" },
  { label: "GameMaker", value: "GAMEMAKER" },
  { label: "CryEngine", value: "CRYENGINE" },
  { label: "Blender", value: "BLENDER" },

  // 🤖 Artificial Intelligence / Machine Learning / Data
  { label: "Artificial Intelligence", value: "ARTIFICIAL INTELLIGENCE" },
  { label: "Machine Learning", value: "MACHINE LEARNING" },
  { label: "Deep Learning", value: "DEEP LEARNING" },
  { label: "Neural Networks", value: "NEURAL NETWORKS" },
  { label: "Computer Vision", value: "COMPUTER VISION" },
  {
    label: "Natural Language Processing",
    value: "NATURAL LANGUAGE PROCESSING",
  },
  { label: "Reinforcement Learning", value: "REINFORCEMENT LEARNING" },
  { label: "Generative AI", value: "GENERATIVE AI" },
  { label: "LLM (Large Language Models)", value: "LLM" },
  { label: "Data Science", value: "DATA SCIENCE" },
  { label: "Big Data", value: "BIG DATA" },
  { label: "Data Engineering", value: "DATA ENGINEERING" },
  { label: "MLOps", value: "MLOPS" },
  { label: "Data Analytics", value: "DATA ANALYTICS" },
  { label: "Business Intelligence", value: "BUSINESS INTELLIGENCE" },

  // ⚙️ AI Tools & Frameworks
  { label: "Python", value: "PYTHON" },
  { label: "R", value: "R" },
  { label: "TensorFlow", value: "TENSORFLOW" },
  { label: "PyTorch", value: "PYTORCH" },
  { label: "Keras", value: "KERAS" },
  { label: "Scikit-learn", value: "SCIKIT-LEARN" },
  { label: "Pandas", value: "PANDAS" },
  { label: "NumPy", value: "NUMPY" },
  { label: "Matplotlib", value: "MATPLOTLIB" },
  { label: "Seaborn", value: "SEABORN" },
  { label: "OpenCV", value: "OPENCV" },
  { label: "NLTK", value: "NLTK" },
  { label: "spaCy", value: "SPACY" },
  { label: "Hugging Face", value: "HUGGING FACE" },
  { label: "LangChain", value: "LANGCHAIN" },
  { label: "Transformers", value: "TRANSFORMERS" },
  { label: "Jupyter", value: "JUPYTER" },
  { label: "Apache Spark", value: "APACHE SPARK" },
  { label: "Hadoop", value: "HADOOP" },

  // 🌐 Web Development
  { label: "HTML", value: "HTML" },
  { label: "CSS", value: "CSS" },
  { label: "JavaScript", value: "JAVASCRIPT" },
  { label: "TypeScript", value: "TYPESCRIPT" },
  { label: "React.JS", value: "REACT.JS" },
  { label: "Vue.JS", value: "VUE.JS" },
  { label: "Angular", value: "ANGULAR" },
  { label: "Svelte", value: "SVELTE" },
  { label: "Next.JS", value: "NEXT.JS" },
  { label: "Nuxt.JS", value: "NUXT.JS" },
  { label: "Astro", value: "ASTRO" },
  { label: "TailwindCSS", value: "TAILWINDCSS" },
  { label: "Bootstrap", value: "BOOTSTRAP" },
  { label: "Material UI", value: "MATERIAL UI" },
  { label: "Chakra UI", value: "CHAKRA UI" },

  // Backend & Server
  { label: "Node.JS", value: "NODE.JS" },
  { label: "Express.JS", value: "EXPRESS.JS" },
  { label: "Nest.JS", value: "NEST.JS" },
  { label: "Django", value: "DJANGO" },
  { label: "Flask", value: "FLASK" },
  { label: "FastAPI", value: "FASTAPI" },
  { label: "Spring Boot", value: "SPRING BOOT" },
  { label: "Ruby on Rails", value: "RUBY ON RAILS" },
  { label: "Laravel", value: "LARAVEL" },
  { label: "PHP", value: "PHP" },
  { label: "Go", value: "GO" },
  { label: "Rust", value: "RUST" },

  // Databases
  { label: "MySQL", value: "MYSQL" },
  { label: "PostgreSQL", value: "POSTGRESQL" },
  { label: "MongoDB", value: "MONGODB" },
  { label: "Redis", value: "REDIS" },
  { label: "SQLite", value: "SQLITE" },
  { label: "Oracle DB", value: "ORACLE DB" },
  { label: "MariaDB", value: "MARIADB" },
  { label: "Firebase", value: "FIREBASE" },
  { label: "Supabase", value: "SUPABASE" },

  // DevOps & Cloud
  { label: "Docker", value: "DOCKER" },
  { label: "Kubernetes", value: "KUBERNETES" },
  { label: "Jenkins", value: "JENKINS" },
  { label: "GitHub Actions", value: "GITHUB ACTIONS" },
  { label: "AWS", value: "AWS" },
  { label: "Azure", value: "AZURE" },
  { label: "Google Cloud", value: "GOOGLE CLOUD" },
  { label: "CI/CD", value: "CI/CD" },
  { label: "Terraform", value: "TERRAFORM" },

  // 📱 Mobile Development
  { label: "React Native", value: "REACT NATIVE" },
  { label: "Flutter", value: "FLUTTER" },
  { label: "Kotlin", value: "KOTLIN" },
  { label: "Swift", value: "SWIFT" },
  { label: "Java", value: "JAVA" },
  { label: "Objective-C", value: "OBJECTIVE-C" },
  { label: "Xamarin", value: "XAMARIN" },
  { label: "Ionic", value: "IONIC" },
];

export const LOCATION_LIST = [
  { label: "Hà Nội", value: "HANOI" },
  { label: "Hồ Chí Minh", value: "HOCHIMINH" },
  { label: "Đà Nẵng", value: "DANANG" },
  { label: "Others", value: "OTHER" },
  { label: "Tất cả thành phố", value: "ALL" },
];

export const nonAccentVietnamese = (str: string) => {
  str = str.replace(/A|Á|À|Ã|Ạ|Â|Ấ|Ầ|Ẫ|Ậ|Ă|Ắ|Ằ|Ẵ|Ặ/g, "A");
  str = str.replace(/à|á|ạ|ả|ã|â|ầ|ấ|ậ|ẩ|ẫ|ă|ằ|ắ|ặ|ẳ|ẵ/g, "a");
  str = str.replace(/E|É|È|Ẽ|Ẹ|Ê|Ế|Ề|Ễ|Ệ/, "E");
  str = str.replace(/è|é|ẹ|ẻ|ẽ|ê|ề|ế|ệ|ể|ễ/g, "e");
  str = str.replace(/I|Í|Ì|Ĩ|Ị/g, "I");
  str = str.replace(/ì|í|ị|ỉ|ĩ/g, "i");
  str = str.replace(/O|Ó|Ò|Õ|Ọ|Ô|Ố|Ồ|Ỗ|Ộ|Ơ|Ớ|Ờ|Ỡ|Ợ/g, "O");
  str = str.replace(/ò|ó|ọ|ỏ|õ|ô|ồ|ố|ộ|ổ|ỗ|ơ|ờ|ớ|ợ|ở|ỡ/g, "o");
  str = str.replace(/U|Ú|Ù|Ũ|Ụ|Ư|Ứ|Ừ|Ữ|Ự/g, "U");
  str = str.replace(/ù|ú|ụ|ủ|ũ|ư|ừ|ứ|ự|ử|ữ/g, "u");
  str = str.replace(/Y|Ý|Ỳ|Ỹ|Ỵ/g, "Y");
  str = str.replace(/ỳ|ý|ỵ|ỷ|ỹ/g, "y");
  str = str.replace(/Đ/g, "D");
  str = str.replace(/đ/g, "d");
  // Some system encode vietnamese combining accent as individual utf-8 characters
  str = str.replace(/\u0300|\u0301|\u0303|\u0309|\u0323/g, ""); // Huyền sắc hỏi ngã nặng
  str = str.replace(/\u02C6|\u0306|\u031B/g, ""); // Â, Ê, Ă, Ơ, Ư
  return str;
};

export const convertSlug = (str: string) => {
  str = nonAccentVietnamese(str);
  str = str.replace(/^\s+|\s+$/g, ""); // trim
  str = str.toLowerCase();

  // remove accents, swap ñ for n, etc
  const from =
    "ÁÄÂÀÃÅČÇĆĎÉĚËÈÊẼĔȆĞÍÌÎÏİŇÑÓÖÒÔÕØŘŔŠŞŤÚŮÜÙÛÝŸŽáäâàãåčçćďéěëèêẽĕȇğíìîïıňñóöòôõøðřŕšşťúůüùûýÿžþÞĐđßÆa·/_,:;";
  const to =
    "AAAAAACCCDEEEEEEEEGIIIIINNOOOOOORRSSTUUUUUYYZaaaaaacccdeeeeeeeegiiiiinnooooooorrsstuuuuuyyzbBDdBAa------";
  for (let i = 0, l = from.length; i < l; i++) {
    str = str.replace(new RegExp(from.charAt(i), "g"), to.charAt(i));
  }

  str = str
    .replace(/[^a-z0-9 -]/g, "") // remove invalid chars
    .replace(/\s+/g, "-") // collapse whitespace and replace by -
    .replace(/-+/g, "-"); // collapse dashes

  return str;
};

export const getLocationName = (value: string) => {
  const locationFilter = LOCATION_LIST.filter((item) => item.value === value);
  if (locationFilter.length) return locationFilter[0].label;
  return "unknown";
};

export function colorMethod(
  method: "POST" | "PUT" | "GET" | "DELETE" | string
) {
  switch (method) {
    case "POST":
      return green[6];
    case "PUT":
      return orange[6];
    case "GET":
      return blue[6];
    case "DELETE":
      return red[6];
    default:
      return grey[10];
  }
}
