import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.variables.IntVar;

import java.io.File;
import java.util.Scanner;

public class ClassSchedule_Choco {
    int U, K;
    int N; // Số lớp học
    int M; // Số phòng học
    int[] s; // sĩ số các lớp
    int[] g; // giáo viên
    int[] c; // số chỗ ngồi của các phòng
    int[] t; // tổng số tiết của các lớp trong tuần

    Model model = null;
    IntVar [][][][] x = null;

    public void build_model() {
        input("./data/data.txt");
        model = new Model();

        x = new IntVar[N][U][K][M];

        // 1. Lớp học không thể xếp vào phòng có sức chứa nhỏ hơn số lượng sinh viên
        for (int i = 0; i < N; i++) {
            for (int u = 0; u < U; u++) {
                for (int k = 0; k < K; k++) {
                    for (int j = 0; j < M; j++) {
                        // sức chứa : nếu s[i] >= c[j] thì x[i][u][k][j] = 0
                        if (s[i] > c[j])
                            x[i][u][k][j] = model.intVar(0, 0);
                        else x[i][u][k][j] = model.intVar(0, 1);
                    }
                }
            }
        }

        // 2. Trong một thời điểm, một lớp chỉ có thể học tại 1 phòng
        int[] oneM = new int[M];
        for (int j = 0; j < M; j++) {
            oneM[j] = 1;
        }
        for (int i = 0; i < N; i++) {
            for (int u = 0; u < U; u++) {
                for (int k = 0; k < K; k++) {
                    model.scalar(x[i][u][k], oneM, "<=", 1).post();
                }
            }
        }

        // 3. Trong cùng thời điểm, không thể có 2 lớp học cùng 1 phòng
        for (int i1 = 0; i1 < N-1; i1++) {
            for (int i2 = i1 + 1; i2 < N; i2++) {
                for (int u = 0; u < U; u++) {
                    for (int k = 0; k < K; k++) {
                        for (int j = 0; j < M; j++) {
                            model.arithm(x[i1][u][k][j], "+", x[i2][u][k][j], "<=", 1).post();
                        }

                    }
                }
            }
        }

        // 4. Trong cùng thời điểm, không thể có 2 lớp cùng giáo viên
        for (int i1 = 0; i1 < N-1; i1++) {
            for (int i2 = i1 + 1; i2 < N; i2++) {
                if  (g[i1] == g[i2]) {
                    for (int u = 0; u < U; u++) {
                        for (int k = 0; k < K; k++) {
                            for (int j1 = 0; j1 < M; j1++) {
                                for (int j2 = j1 + 1; j2 < M; j2++) {
                                    model.arithm(x[i1][u][k][j1], "+", x[i2][u][k][j2], "<=", 1).post();
                                }

                            }

                        }
                    }
                }

            }
        }

        // 5. Tổng tất cả các tiết học phải được bảo toàn
        int[] oneZ = new int[U*K*M];
        for (int i = 0; i < U*K*M; i++) oneZ[i] = 1;

        for (int i = 0; i < N; i++) {
            IntVar[] tmp = new IntVar[U*K*M];
            int idx = 0;
            for (int u = 0; u < U; u++) {
                for (int k = 0; k < K; k++) {
                    for (int j = 0; j < M; j++) {
                        tmp[idx] = x[i][u][k][j];
                        idx++;
                    }
                }
            }
            model.scalar(tmp, oneZ, "=", t[i]).post();
        }

        Solver solver = model.getSolver();
        solver.limitTime("2h");
        if (!solver.solve()) {
            System.out.println("No solution!");
        }

        // In giải pháp
        System.out.println("lop: thu-tiet-phong");
        for (int i = 0; i < N; i++) {
            System.out.println();
            System.out.print("lop " + (i+1) + " : ");
            for (int u = 0; u < U; u++) {
                for (int k = 0; k < K; k++) {
                    for (int j = 0; j < M; j++) {
                        if (x[i][u][k][j].getValue() == 1) {
                            System.out.print((u+2) + "-" + (k+1) + "-" + (j+1) + ", ");
                        }
                    }
                }
            }
        }
    }

    private void input(String file_name){
        try {
            File f = new File(file_name);
            Scanner scanner = new Scanner(f);

            N = scanner.nextInt();
            M = scanner.nextInt();
            U = scanner.nextInt();
            K = scanner.nextInt();

            t = new int[N];
            g = new int[N];
            s = new int[N];
            c = new int[M];
            for (int i = 0; i < N; i++) {
                t[i] = scanner.nextInt();
                g[i] = scanner.nextInt();
                s[i] = scanner.nextInt();
            }
            for (int i = 0; i < M; i++) {
                c[i] = scanner.nextInt();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        final long startTime = System.currentTimeMillis();
        ClassSchedule_Choco app = new ClassSchedule_Choco();
        app.build_model();
        final long duration = System.currentTimeMillis() - startTime;
        long minutes = (duration / 1000) / 60;
        long seconds  = (duration / 1000) % 60;
        System.out.println("\n");
        System.out.println(duration + "ms");
        System.out.println("\n" + minutes + "phút " + seconds + "s");
    }
}
