/*
	Launch4j (http://launch4j.sourceforge.net/)
    Modified for VocalShimeji ScreenSave Mode
*/

#include "../resource.h"
#include "../head.h"
#include "scrnsavehead.h"
#define ISNUM(c) ((c) >= '0' && c <= '9')

static int ISSPACE(char c)
{
    return (c == ' ' || c == '\t');
}

extern FILE* hLog;
extern PROCESS_INFORMATION pi;
extern char args[];
HWND hWnd;
DWORD dwExitCode = 0;
BOOL stayAlive = FALSE;
BOOL splash = FALSE;
BOOL splashTimeoutErr;
BOOL waitForWindow;
BOOL quitOnInput = FALSE;
RECT rc;
HINSTANCE	hMainInstance;
char szVersion[] = TEXT("VocalShimeji\r\nScreen Saver Mode\r\n(build 20120220)");

LRESULT CALLBACK PreviewProc(HWND hWnd, UINT message, WPARAM wParam, LPARAM lParam)
{
    PAINTSTRUCT ps;
    HDC hdc;
    RECT rect;
    switch (message)
    {
    case WM_PAINT:
        hdc = BeginPaint(hWnd, &ps);
        GetClientRect(hWnd, &rect);
        if(loadBool(SHOW_SPLASH))
        {
            HANDLE hImage = LoadImage(hMainInstance,	// handle of the instance containing the image
                                      MAKEINTRESOURCE(SPLASH_BITMAP),	// name or identifier of image
                                      IMAGE_BITMAP,					// type of image
                                      rc.right,								// desired width
                                      rc.bottom,								// desired height
                                      LR_DEFAULTSIZE);
            if (hImage == NULL)
            {
                signalError();
                return 1;
            }
            BITMAP bm;
            GetObject(hImage, sizeof(bm), &bm);
            HDC mdc;
            mdc = CreateCompatibleDC(hdc);
            HBITMAP oldbmp;
            oldbmp = (HBITMAP)SelectObject(mdc, (HGDIOBJ)hImage);
            BitBlt(hdc, 0, 0, bm.bmWidth, bm.bmHeight, mdc, 0, 0, SRCCOPY);
            SelectObject(mdc, (HGDIOBJ)oldbmp);
        }
        else
        {
            DrawText(hdc, szVersion, strlen(szVersion), &rc, DT_CENTER);
        }
        EndPaint(hWnd, &ps);
        break;
    case WM_CREATE:
        break;
    case WM_DESTROY:
        PostQuitMessage(0);
        break;
    default:
        return DefWindowProc(hWnd, message, wParam, lParam);
    }
    return 0;
}

static int DrawPreview(HWND hParent)
{
    WNDCLASS cls;

    cls.hCursor = NULL;
    cls.hIcon = NULL;
    cls.lpszMenuName = NULL;
    cls.lpszClassName = "WindowsScreenSaverClass";
    cls.hbrBackground = GetStockObject(WHITE_BRUSH);
    cls.hInstance = hMainInstance;
    cls.style = CS_VREDRAW | CS_HREDRAW | CS_SAVEBITS | CS_PARENTDC;
    cls.lpfnWndProc = (WNDPROC) PreviewProc;
    cls.cbWndExtra = 0;
    cls.cbClsExtra = 0;
    RegisterClass(&cls);

    HWND	hMainWindow = NULL;
    UINT style;
    MSG msg;
    style = WS_CHILD;
    GetClientRect(hParent, &rc);
    hMainWindow = CreateWindowEx( 0, "WindowsScreenSaverClass",
                                  szVersion, style,
                                  0, 0, rc.right, rc.bottom, hParent, NULL,
                                  hMainInstance, NULL);

    /* display window and start pumping messages */
    if (hMainWindow)
    {
        UpdateWindow(hMainWindow);
        ShowWindow(hMainWindow, SW_SHOW);

        while (GetMessage(&msg, NULL, 0, 0) == TRUE)
        {
            TranslateMessage(&msg);
            DispatchMessage(&msg);
        }
    }
    return 0;
}
static unsigned long
_toul(const char *s)
{
    unsigned long res;
    unsigned long n;
    const char *p;
    for (p = s; *p; p++)
        if (!ISNUM(*p)) break;
    p--;
    res = 0;
    for (n = 1; p >= s; p--, n *= 10)
        res += (*p - '0') * n;
    return res;
}

/* screen saver entry point */
int APIENTRY WinMain(HINSTANCE hInst, HINSTANCE hPrevInst,
                     LPSTR CmdLine, int nCmdShow)
{
    LPSTR p;
    hMainInstance = hInst;

    for (p = CmdLine; *p; p++)
    {
        switch (*p)
        {
        case 'S':
        case 's':
            /* start screen saver */
            quitOnInput = TRUE;
            return l4j_WinMain(hInst,hPrevInst,"--ScrnSave",nCmdShow);
        case 'P':
        case 'p':
        {
            /* start screen saver in preview window */
            HWND hParent;
            while (ISSPACE(*++p));
            hParent = (HWND) _toul(p);
            if (hParent && IsWindow(hParent))
                return DrawPreview(hParent);
            return 0;
        }
        return 0;

        case 'C':
        case 'c':
            /* display configure dialog */
            return l4j_WinMain(hInst,hPrevInst,"--ShowConfig",nCmdShow);
        case 'A':
        case 'a':
        {
            /* change screen saver password */
            MessageBox(NULL, "CHANGE PASSWORD IS NOT SUPPORT", "WARNING", MB_OK);
        }
        return 0;

        case '-':
        case '/':
        case ' ':
        default:
            break;
        }
    }
    //
    return l4j_WinMain(hInst,hPrevInst,"--ShowConfig",nCmdShow);
}

int l4j_WinMain(HINSTANCE hInstance,
                HINSTANCE hPrevInstance,
                LPSTR     lpCmdLine,
                int       nCmdShow)
{
    int result = prepare(lpCmdLine);
    if (result == ERROR_ALREADY_EXISTS)
    {
        HWND handle = getInstanceWindow();
        ShowWindow(handle, SW_SHOW);
        SetForegroundWindow(handle);
        closeLogFile();
        return 2;
    }
    if (result != TRUE)
    {
        signalError();
        return 1;
    }
    splash = FALSE;
    /* splash = loadBool(SHOW_SPLASH)
     *		&& strstr(lpCmdLine, "--l4j-no-splash") == NULL;
     */
    stayAlive = TRUE;
    /* stayAlive = loadBool(GUI_HEADER_STAYS_ALIVE)
     *		&& strstr(lpCmdLine, "--l4j-dont-wait") == NULL;
     */
    if (splash || stayAlive)
    {

        hWnd = CreateWindowEx(WS_EX_TOOLWINDOW, "STATIC", "",
                              WS_POPUP | SS_BITMAP,
                              0, 0, CW_USEDEFAULT, CW_USEDEFAULT, NULL, NULL, hInstance, NULL);
        if (!SetTimer (hWnd, ID_TIMER, 1000 /* 1s */, TimerProc))
        {
            signalError();
            return 1;
        }

    }
    if (execute(FALSE) == -1)
    {
        signalError();
        return 1;
    }
    if (!(splash || stayAlive))
    {
        debug("Exit code:\t0\n");
        closeHandles();
        return 0;
    }

    MSG msg;
    while (GetMessage(&msg, NULL, 0, 0))
    {
        TranslateMessage(&msg);
        DispatchMessage(&msg);
    }

    debug("Exit code:\t%d\n", dwExitCode);
    closeHandles();
    return dwExitCode;
}

HWND getInstanceWindow()
{
    char windowTitle[STR];
    char instWindowTitle[STR] = {0};
    if (loadString(INSTANCE_WINDOW_TITLE, instWindowTitle))
    {
        HWND handle = FindWindowEx(NULL, NULL, NULL, NULL);
        while (handle != NULL)
        {
            GetWindowText(handle, windowTitle, STR - 1);
            if (strstr(windowTitle, instWindowTitle) != NULL)
            {
                return handle;
            }
            else
            {
                handle = FindWindowEx(NULL, handle, NULL, NULL);
            }
        }
    }
    return NULL;
}

BOOL CALLBACK enumwndfn(HWND hwnd, LPARAM lParam)
{
    DWORD processId;
    GetWindowThreadProcessId(hwnd, &processId);
    if (pi.dwProcessId == processId)
    {
        LONG styles = GetWindowLong(hwnd, GWL_STYLE);
        if ((styles & WS_VISIBLE) != 0)
        {
            splash = FALSE;
            ShowWindow(hWnd, SW_HIDE);
            return FALSE;
        }
    }
    return TRUE;
}


DWORD lastInputTime = 0;

VOID CALLBACK TimerProc(
    HWND hwnd,			// handle of window for timer messages
    UINT uMsg,			// WM_TIMER message
    UINT idEvent,		// timer identifier
    DWORD dwTime)  		// current system time
{
    GetExitCodeProcess(pi.hProcess, &dwExitCode);
    if (dwExitCode != STILL_ACTIVE)
    {
        KillTimer(hWnd, ID_TIMER);
        PostQuitMessage(0);
    }
    if(quitOnInput){
    LASTINPUTINFO lpi;
    lpi.cbSize = sizeof(lpi);
    GetLastInputInfo(&lpi);
    if (lastInputTime && lastInputTime != lpi.dwTime){
        TerminateProcess(pi.hProcess,0);
    }
    lastInputTime = lpi.dwTime;
    }
}
